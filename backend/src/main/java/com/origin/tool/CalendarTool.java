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
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 start datetime (e.g. YYYY-MM-DDTHH:mm:ssZ), calculated relative to current system time") String startTime,
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 end datetime (e.g. YYYY-MM-DDTHH:mm:ssZ), calculated relative to current system time") String endTime,
        @JsonPropertyDescription("Optional description") String description,
        @JsonPropertyDescription("Optional location") String location
    ) {}

    public record ListCalendarEventsRequest() {}

    public record DeleteCalendarEventRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID or title/name of the event to delete") String eventId
    ) {}

    public String createCalendarEvent(CreateCalendarEventRequest req) {
        try {
            CalendarEvent event = new CalendarEvent();
            event.setUserId(userId);
            event.setTitle(req.title());
            Instant start = com.origin.util.DateTimeUtil.parseInstant(req.startTime());
            Instant end = req.endTime() != null && !req.endTime().isBlank()
                ? com.origin.util.DateTimeUtil.parseInstant(req.endTime())
                : start.plus(1, java.time.temporal.ChronoUnit.HOURS);
            event.setStartTime(start);
            event.setEndTime(end);
            event.setDescription(req.description());
            event.setLocation(req.location());
            CalendarEvent saved = calendarEventRepository.save(event);
            return "Calendar event scheduled: '" + saved.getTitle() + "' from " + saved.getStartTime() + " to " + saved.getEndTime() + ".";
        } catch (Exception e) {
            return "Failed to create calendar event: " + e.getMessage();
        }
    }

    public String listCalendarEvents(ListCalendarEventsRequest req) {
        try {
            List<CalendarEvent> events = calendarEventRepository
                .findByUserIdOrderByStartTimeAsc(userId);
            if (events.isEmpty()) return "No calendar events found in schedule.";
            Instant now = Instant.now();
            return events.stream()
                .map(e -> {
                    String timeStatus = e.getEndTime() != null && e.getEndTime().isBefore(now)
                        ? "[PAST]"
                        : (e.getStartTime() != null && e.getStartTime().isBefore(now) ? "[HAPPENING NOW]" : "[UPCOMING]");
                    return "- %s '%s' (%s to %s)%s%s".formatted(
                        timeStatus,
                        e.getTitle(),
                        e.getStartTime(), e.getEndTime(),
                        e.getLocation() != null && !e.getLocation().isBlank() ? " @ " + e.getLocation() : "",
                        e.getDescription() != null && !e.getDescription().isBlank() ? " (" + e.getDescription() + ")" : "");
                })
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list calendar events: " + e.getMessage();
        }
    }

    public String deleteCalendarEvent(DeleteCalendarEventRequest req) {
        try {
            try {
                UUID id = UUID.fromString(req.eventId());
                if (!calendarEventRepository.existsById(id)) {
                    return "No calendar event found with ID: " + req.eventId();
                }
                calendarEventRepository.deleteById(id);
                return "Calendar event deleted successfully.";
            } catch (IllegalArgumentException notUuid) {
                List<CalendarEvent> matches = calendarEventRepository.findByUserIdOrderByStartTimeAsc(userId)
                    .stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(req.eventId().toLowerCase()))
                    .toList();
                if (!matches.isEmpty()) {
                    CalendarEvent evt = matches.get(0);
                    calendarEventRepository.deleteById(evt.getId());
                    return "Calendar event '" + evt.getTitle() + "' deleted successfully.";
                }
                return "No calendar event found matching '" + req.eventId() + "'.";
            }
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
                .withDescription("Retrieves all calendar events and schedule from the database (including upcoming, current, and past). Call this tool whenever the user asks to see, check, fetch, or list their schedule or meetings.")
                .withInputType(ListCalendarEventsRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::deleteCalendarEvent)
                .withName("deleteCalendarEvent")
                .withDescription("Deletes a calendar event by its title/name or UUID.")
                .withInputType(DeleteCalendarEventRequest.class)
                .build()
        );
    }
}
