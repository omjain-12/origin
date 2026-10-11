package com.origin.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Customizes Spring's RestClient to handle:
 * 1. Double /v1/v1/ URLs when BASE_URL ends with /v1
 * 2. Buffering response body so it can never be prematurely closed or exhausted
 * 3. Normalizing Content-Type: application/octet-stream from Bedrock Mantle to application/json
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            restClientBuilder.requestInterceptor((request, body, execution) -> {
                // 1. Fix double /v1/v1/ in URL if base-url included /v1
                HttpRequest actualRequest = request;
                String uriStr = request.getURI().toString();
                if (uriStr.contains("/v1/v1/")) {
                    URI fixedUri = URI.create(uriStr.replace("/v1/v1/", "/v1/"));
                    actualRequest = new HttpRequestWrapper(request) {
                        @Override
                        public URI getURI() {
                            return fixedUri;
                        }
                    };
                }

                ClientHttpResponse response = execution.execute(actualRequest, body);
                return new OctetStreamToJsonResponseWrapper(response);
            });

            restClientBuilder.messageConverters(converters -> {
                for (HttpMessageConverter<?> converter : converters) {
                    if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                        List<MediaType> supported = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                        if (!supported.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                            supported.add(MediaType.APPLICATION_OCTET_STREAM);
                        }
                        jacksonConverter.setSupportedMediaTypes(supported);
                    }
                }
            });
        };
    }

    private static class OctetStreamToJsonResponseWrapper implements ClientHttpResponse {
        private final ClientHttpResponse delegate;
        private final HttpHeaders headers;
        private final byte[] bodyBytes;

        public OctetStreamToJsonResponseWrapper(ClientHttpResponse delegate) throws IOException {
            this.delegate = delegate;
            this.headers = new HttpHeaders();
            this.headers.putAll(delegate.getHeaders());
            MediaType contentType = delegate.getHeaders().getContentType();
            if (contentType == null || MediaType.APPLICATION_OCTET_STREAM.includes(contentType)) {
                this.headers.setContentType(MediaType.APPLICATION_JSON);
            }
            try (InputStream is = delegate.getBody()) {
                this.bodyBytes = is != null ? is.readAllBytes() : new byte[0];
            }
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(this.bodyBytes);
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
