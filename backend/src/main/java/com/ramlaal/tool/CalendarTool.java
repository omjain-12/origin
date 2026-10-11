package com.ramlaal.tool;

import com.ramlaal.domain.entity.CalendarEvent;
import com.ramlaal.domain.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CalendarTool {

    private final CalendarEventRepository calendarEventRepository;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    @Tool(description = "Creates a new calendar event. Returns the created event's ID.")
    public String createCalendarEvent(
        @ToolParam(description = "Event title") String title,
        @ToolParam(description = "ISO-8601 start datetime (e.g. 2026-10-15T16:00:00Z)") String startTime,
        @ToolParam(description = "ISO-8601 end datetime (e.g. 2026-10-15T17:00:00Z)") String endTime,
        @ToolParam(description = "Optional description") String description,
        @ToolParam(description = "Optional location") String location
    ) {
        try {
            CalendarEvent event = new CalendarEvent();
            event.setUserId(userId);
            event.setTitle(title);
            event.setStartTime(Instant.parse(startTime));
            event.setEndTime(Instant.parse(endTime));
            event.setDescription(description);
            event.setLocation(location);
            CalendarEvent saved = calendarEventRepository.save(event);
            return "Calendar event created with ID: " + saved.getId() + " — Title: " + saved.getTitle();
        } catch (Exception e) {
            return "Failed to create calendar event: " + e.getMessage();
        }
    }

    @Tool(description = "Lists upcoming calendar events for the user.")
    public String listCalendarEvents() {
        try {
            List<CalendarEvent> events = calendarEventRepository
                .findByUserIdAndStartTimeAfterOrderByStartTimeAsc(userId, Instant.now());
            if (events.isEmpty()) return "No upcoming calendar events found.";
            return events.stream()
                .map(e -> "[%s] %s — %s to %s%s".formatted(
                    e.getId(), e.getTitle(),
                    e.getStartTime(), e.getEndTime(),
                    e.getLocation() != null ? " @ " + e.getLocation() : ""))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list calendar events: " + e.getMessage();
        }
    }

    @Tool(description = "Deletes a calendar event by its ID.")
    public String deleteCalendarEvent(
        @ToolParam(description = "UUID of the event to delete") String eventId
    ) {
        try {
            UUID id = UUID.fromString(eventId);
            if (!calendarEventRepository.existsById(id)) {
                return "No calendar event found with ID: " + eventId;
            }
            calendarEventRepository.deleteById(id);
            return "Calendar event " + eventId + " deleted successfully.";
        } catch (Exception e) {
            return "Failed to delete calendar event: " + e.getMessage();
        }
    }
}
