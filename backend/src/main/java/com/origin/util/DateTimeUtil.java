package com.origin.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class DateTimeUtil {

    private DateTimeUtil() {}

    public static Instant parseInstant(String text) {
        if (text == null || text.isBlank()) {
            return Instant.now().plus(1, ChronoUnit.HOURS);
        }
        String s = text.trim();

        // 1. Standard ISO-8601 with timezone (e.g. 2026-10-15T15:50:00Z, 2026-10-15T15:50:00+05:30)
        try {
            return Instant.parse(s);
        } catch (DateTimeParseException ignored) {}

        // 2. ISO LocalDateTime without timezone (e.g. 2026-10-15T15:50:00, 2026-10-15T15:50)
        try {
            LocalDateTime ldt = LocalDateTime.parse(s);
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException ignored) {}

        // 3. Space-separated datetime (e.g. 2026-10-15 15:50:00, 2026-10-15 15:50)
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");
            LocalDateTime ldt = LocalDateTime.parse(s, formatter);
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException ignored) {}

        // 4. Date only (e.g. 2026-10-15)
        try {
            LocalDate ld = LocalDate.parse(s);
            return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException ignored) {}

        // 5. Relative phrases
        String lower = s.toLowerCase();
        if (lower.startsWith("in ") && lower.contains("min")) {
            String digits = lower.replaceAll("\\D+", "");
            if (!digits.isEmpty()) {
                return Instant.now().plus(Long.parseLong(digits), ChronoUnit.MINUTES);
            }
        }
        if (lower.startsWith("in ") && lower.contains("hour")) {
            String digits = lower.replaceAll("\\D+", "");
            if (!digits.isEmpty()) {
                return Instant.now().plus(Long.parseLong(digits), ChronoUnit.HOURS);
            }
        }
        if (lower.startsWith("in ") && lower.contains("day")) {
            String digits = lower.replaceAll("\\D+", "");
            if (!digits.isEmpty()) {
                return Instant.now().plus(Long.parseLong(digits), ChronoUnit.DAYS);
            }
        }

        return Instant.now().plus(1, ChronoUnit.HOURS);
    }
}

