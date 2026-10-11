package com.origin.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.origin.domain.entity.TaskItem;
import com.origin.domain.repository.TaskItemRepository;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskTool {

    private final TaskItemRepository taskItemRepository;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public TaskTool(TaskItemRepository taskItemRepository) {
        this.taskItemRepository = taskItemRepository;
    }

    public record CreateTaskRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("Task title") String title,
        @JsonPropertyDescription("Optional description") String description,
        @JsonPropertyDescription("Priority: LOW, MEDIUM, HIGH, or URGENT") String priority,
        @JsonPropertyDescription("Optional ISO-8601 due date/time (e.g. YYYY-MM-DDTHH:mm:ssZ), calculated relative to current system time") String dueDate
    ) {}

    public record ListTasksRequest() {}

    public record CompleteTaskRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID or title/name of the task to mark completed") String taskId
    ) {}

    public record DeleteTaskRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID or title/name of the task to delete") String taskId
    ) {}

    public String createTask(CreateTaskRequest req) {
        try {
            TaskItem task = new TaskItem();
            task.setUserId(userId);
            task.setTitle(req.title());
            task.setDescription(req.description());
            String prio = req.priority() != null ? req.priority().toUpperCase() : "MEDIUM";
            task.setPriority(TaskItem.Priority.valueOf(prio));
            if (req.dueDate() != null && !req.dueDate().isBlank()) {
                task.setDueDate(com.origin.util.DateTimeUtil.parseInstant(req.dueDate()));
            }
            TaskItem saved = taskItemRepository.save(task);
            return "Task created: '" + saved.getTitle() + "' with " + saved.getPriority() + " priority" + (saved.getDueDate() != null ? " (Due: " + saved.getDueDate() + ")" : "") + ".";
        } catch (Exception e) {
            return "Failed to create task: " + e.getMessage();
        }
    }

    public String listTasks(ListTasksRequest req) {
        try {
            List<TaskItem> tasks = taskItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
            if (tasks.isEmpty()) return "No tasks found in database.";
            return tasks.stream()
                .map(t -> "- [%s Priority] %s (Status: %s)%s".formatted(
                    t.getPriority(),
                    t.getTitle(),
                    t.getStatus(),
                    t.getDescription() != null && !t.getDescription().isBlank() ? " — " + t.getDescription() : ""))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list tasks: " + e.getMessage();
        }
    }

    public String completeTask(CompleteTaskRequest req) {
        try {
            try {
                UUID id = UUID.fromString(req.taskId());
                return taskItemRepository.findById(id).map(task -> {
                    task.setStatus(TaskItem.Status.COMPLETED);
                    taskItemRepository.save(task);
                    return "Task '" + task.getTitle() + "' marked as completed.";
                }).orElse("No task found with ID: " + req.taskId());
            } catch (IllegalArgumentException notUuid) {
                List<TaskItem> matches = taskItemRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(req.taskId().toLowerCase()))
                    .toList();
                if (!matches.isEmpty()) {
                    TaskItem task = matches.get(0);
                    task.setStatus(TaskItem.Status.COMPLETED);
                    taskItemRepository.save(task);
                    return "Task '" + task.getTitle() + "' marked as completed.";
                }
                return "No task found matching '" + req.taskId() + "'.";
            }
        } catch (Exception e) {
            return "Failed to complete task: " + e.getMessage();
        }
    }

    public String deleteTask(DeleteTaskRequest req) {
        try {
            try {
                UUID id = UUID.fromString(req.taskId());
                if (!taskItemRepository.existsById(id)) return "No task found with ID: " + req.taskId();
                taskItemRepository.deleteById(id);
                return "Task deleted successfully.";
            } catch (IllegalArgumentException notUuid) {
                List<TaskItem> matches = taskItemRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(req.taskId().toLowerCase()))
                    .toList();
                if (!matches.isEmpty()) {
                    TaskItem task = matches.get(0);
                    taskItemRepository.deleteById(task.getId());
                    return "Task '" + task.getTitle() + "' deleted successfully.";
                }
                return "No task found matching '" + req.taskId() + "'.";
            }
        } catch (Exception e) {
            return "Failed to delete task: " + e.getMessage();
        }
    }

    public List<FunctionCallback> getCallbacks() {
        return List.of(
            FunctionCallbackWrapper.builder(this::createTask)
                .withName("createTask")
                .withDescription("Creates a new task/todo item for the user.")
                .withInputType(CreateTaskRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::listTasks)
                .withName("listTasks")
                .withDescription("Retrieves all tasks and to-do items from the database (including pending, in-progress, completed, and cancelled) with their priority and status. Call this tool whenever the user asks to see, check, fetch, or list tasks.")
                .withInputType(ListTasksRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::completeTask)
                .withName("completeTask")
                .withDescription("Marks a task as completed by its title/name or UUID.")
                .withInputType(CompleteTaskRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::deleteTask)
                .withName("deleteTask")
                .withDescription("Deletes a task by its title/name or UUID.")
                .withInputType(DeleteTaskRequest.class)
                .build()
        );
    }
}
