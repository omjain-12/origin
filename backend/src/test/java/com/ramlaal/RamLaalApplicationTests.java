package com.ramlaal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "AZURE_OPENAI_API_KEY=test-key",
    "AZURE_OPENAI_ENDPOINT=https://test.openai.azure.com/",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class RamLaalApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: Spring context initializes without errors
    }
}
