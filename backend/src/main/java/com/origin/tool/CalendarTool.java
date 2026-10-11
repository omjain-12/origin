package com.origin.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.origin.domain.entity.CalendarEvent;
import com.origin.domain.repository.CalendarEventRepository;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CalendarTool {

    private final CalendarEventRepository calendarEventRepository;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public CalendarTool(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    public record CreateCalendarEventRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("Event title") String title,
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 start datetime (e.g. 2026-10-15T16:00:00Z)") String startTime,
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 end datetime (e.g. 2026-10-15T17:00:00Z)") String endTime,
        @JsonPropertyDescription("Optional description") String description,
        @JsonPropertyDescription("Optional location") String location
    ) {}

    public record ListCalendarEventsRequest() {}

    public record DeleteCalendarEventRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID of the event to delete") String eventId
    ) {}

    public String createCalendarEvent(CreateCalendarEventRequest req) {
        try {
            CalendarEvent event = new CalendarEvent();
            event.setUserId(userId);
            event.setTitle(req.title());
            event.setStartTime(Instant.parse(req.startTime()));
            event.setEndTime(Instant.parse(req.endTime()));
            event.setDescription(req.description());
            event.setLocation(req.location());
            CalendarEvent saved = calendarEventRepository.save(event);
            return "Calendar event created with ID: " + saved.getId() + " — Title: " + saved.getTitle();
        } catch (Exception e) {
            return "Failed to create calendar event: " + e.getMessage();
        }
    }

    public String listCalendarEvents(ListCalendarEventsRequest req) {
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

    public String deleteCalendarEvent(DeleteCalendarEventRequest req) {
        try {
            UUID id = UUID.fromString(req.eventId());
            if (!calendarEventRepository.existsById(id)) {
                return "No calendar event found with ID: " + req.eventId();
            }
            calendarEventRepository.deleteById(id);
            return "Calendar event " + req.eventId() + " deleted successfully.";
        } catch (Exception e) {
            return "Failed to delete calendar event: " + e.getMessage();
        }
    }

    public List<FunctionCallback> getCallbacks() {
        return List.of(
            FunctionCallbackWrapper.builder(this::createCalendarEvent)
                .withName("createCalendarEvent")
                .withDescription("Creates a new calendar event in the user's schedule.")
                .withInputType(CreateCalendarEventRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::listCalendarEvents)
                .withName("listCalendarEvents")
                .withDescription("Lists upcoming calendar events for the user.")
                .withInputType(ListCalendarEventsRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::deleteCalendarEvent)
                .withName("deleteCalendarEvent")
                .withDescription("Deletes a calendar event by its UUID.")
                .withInputType(DeleteCalendarEventRequest.class)
                .build()
        );
    }
}
