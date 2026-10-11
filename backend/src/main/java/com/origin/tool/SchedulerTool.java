package com.origin.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.origin.domain.entity.ScheduledJobItem;
import com.origin.domain.repository.ScheduledJobItemRepository;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SchedulerTool {

    private final ScheduledJobItemRepository scheduledJobItemRepository;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public SchedulerTool(ScheduledJobItemRepository scheduledJobItemRepository) {
        this.scheduledJobItemRepository = scheduledJobItemRepository;
    }

    public record ScheduleJobRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("Human-readable job name") String jobName,
        @JsonProperty(required = true) @JsonPropertyDescription("Standard 5-field cron expression, e.g. '0 9 * * MON'") String cronExpression,
        @JsonPropertyDescription("Optional description of what this job does") String description
    ) {}

    public record ListJobsRequest() {}

    public String scheduleJob(ScheduleJobRequest req) {
        try {
            if (req.cronExpression() == null || req.cronExpression().trim().split("\\s+").length != 5) {
                return "Invalid cron expression. Must have exactly 5 fields (minute hour day month weekday).";
            }
            ScheduledJobItem job = new ScheduledJobItem();
            job.setUserId(userId);
            job.setJobName(req.jobName());
            job.setCronExpression(req.cronExpression());
            job.setDescription(req.description());
            ScheduledJobItem saved = scheduledJobItemRepository.save(job);
            return "Recurring job '" + saved.getJobName() + "' scheduled successfully (cron: " + req.cronExpression() + ").";
        } catch (Exception e) {
            return "Failed to schedule job: " + e.getMessage();
        }
    }

    public String listJobs(ListJobsRequest req) {
        try {
            List<ScheduledJobItem> jobs = scheduledJobItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
            if (jobs.isEmpty()) return "No scheduled jobs found in database.";
            return jobs.stream()
                .map(j -> "- Job: '%s' (cron: %s, Status: %s)%s".formatted(
                    j.getJobName(),
                    j.getCronExpression(),
                    j.getStatus(),
                    j.getDescription() != null && !j.getDescription().isBlank() ? " — " + j.getDescription() : ""))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list jobs: " + e.getMessage();
        }
    }

    public List<FunctionCallback> getCallbacks() {
        return List.of(
            FunctionCallbackWrapper.builder(this::scheduleJob)
                .withName("scheduleJob")
                .withDescription("Schedules a recurring job using a 5-field cron expression.")
                .withInputType(ScheduleJobRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::listJobs)
                .withName("listJobs")
                .withDescription("Retrieves all recurring and scheduled jobs from the database with their cron expressions and status. Call this tool whenever the user asks to see, check, fetch, or list scheduled jobs.")
                .withInputType(ListJobsRequest.class)
                .build()
        );
    }
}
