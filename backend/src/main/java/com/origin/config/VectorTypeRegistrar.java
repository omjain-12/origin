package com.origin.config;

import com.pgvector.PGvector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class VectorTypeRegistrar {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void registerVectorType() {
        try {
            jdbcTemplate.execute("SELECT 1");
            if (jdbcTemplate.getDataSource() != null) {
                try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
                    PGvector.addVectorType(conn);
                }
            }
        } catch (Exception e) {
            // pgvector type registration is best-effort on startup
        }
    }
}
