package com.origin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "API_KEY=test-key",
    "BASE_URL=https://bedrock-mantle.us-east-1.api.aws/v1",
    "LLM_MODEL=zai.glm-5",
    "EMBEDDING_MODEL=zai.glm-4.7",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class OriginApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: Spring context initializes without errors
    }
}

