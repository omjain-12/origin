package com.ramlaal.tool;

import com.ramlaal.domain.entity.ScheduledJobItem;
import com.ramlaal.domain.repository.ScheduledJobItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SchedulerTool {

    private final ScheduledJobItemRepository scheduledJobItemRepository;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    @Tool(description = "Schedules a recurring job using a cron expression. Returns the job's ID.")
    public String scheduleJob(
        @ToolParam(description = "Human-readable job name") String jobName,
        @ToolParam(description = "Standard 5-field cron expression, e.g. '0 9 * * MON'") String cronExpression,
        @ToolParam(description = "Optional description of what this job does") String description
    ) {
        try {
            // Validate cron field count
            if (cronExpression.trim().split("\\s+").length != 5) {
                return "Invalid cron expression. Must have exactly 5 fields (minute hour day month weekday).";
            }
            ScheduledJobItem job = new ScheduledJobItem();
            job.setUserId(userId);
            job.setJobName(jobName);
            job.setCronExpression(cronExpression);
            job.setDescription(description);
            ScheduledJobItem saved = scheduledJobItemRepository.save(job);
            return "Job scheduled with ID: " + saved.getId() + " — cron: " + cronExpression;
        } catch (Exception e) {
            return "Failed to schedule job: " + e.getMessage();
        }
    }

    @Tool(description = "Lists all active scheduled jobs for the user.")
    public String listJobs() {
        try {
            List<ScheduledJobItem> jobs = scheduledJobItemRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, ScheduledJobItem.Status.ACTIVE);
            if (jobs.isEmpty()) return "No active scheduled jobs.";
            return jobs.stream()
                .map(j -> "[%s] %s — cron: %s".formatted(j.getId(), j.getJobName(), j.getCronExpression()))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list jobs: " + e.getMessage();
        }
    }
}

