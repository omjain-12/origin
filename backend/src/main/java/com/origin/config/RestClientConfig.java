package com.origin.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Customizes Spring's RestClient to handle AWS Bedrock Mantle responses
 * that return Content-Type: application/octet-stream instead of application/json.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            // 1. Intercept responses: if Content-Type is application/octet-stream, rewrite to application/json
            restClientBuilder.requestInterceptor((request, body, execution) -> {
                ClientHttpResponse response = execution.execute(request, body);
                return new OctetStreamToJsonResponseWrapper(response);
            });

            // 2. Also register application/octet-stream on Jackson converter directly as fallback
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

        public OctetStreamToJsonResponseWrapper(ClientHttpResponse delegate) {
            this.delegate = delegate;
            this.headers = new HttpHeaders();
            this.headers.putAll(delegate.getHeaders());
            MediaType contentType = delegate.getHeaders().getContentType();
            if (contentType == null || MediaType.APPLICATION_OCTET_STREAM.includes(contentType)) {
                this.headers.setContentType(MediaType.APPLICATION_JSON);
            }
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public InputStream getBody() throws IOException {
            return delegate.getBody();
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
