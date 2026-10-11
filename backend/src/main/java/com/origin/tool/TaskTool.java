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
        @JsonPropertyDescription("Priority: LOW, MEDIUM, HIGH, or URGENT") String priority
    ) {}

    public record ListTasksRequest() {}

    public record CompleteTaskRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID of the task to mark completed") String taskId
    ) {}

    public record DeleteTaskRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID of the task to delete") String taskId
    ) {}

    public String createTask(CreateTaskRequest req) {
        try {
            TaskItem task = new TaskItem();
            task.setUserId(userId);
            task.setTitle(req.title());
            task.setDescription(req.description());
            String prio = req.priority() != null ? req.priority().toUpperCase() : "MEDIUM";
            task.setPriority(TaskItem.Priority.valueOf(prio));
            TaskItem saved = taskItemRepository.save(task);
            return "Task created with ID: " + saved.getId() + " — " + saved.getTitle();
        } catch (Exception e) {
            return "Failed to create task: " + e.getMessage();
        }
    }

    public String listTasks(ListTasksRequest req) {
        try {
            List<TaskItem> tasks = taskItemRepository
                .findByUserIdAndStatusNotOrderByPriorityDescCreatedAtDesc(userId, TaskItem.Status.CANCELLED);
            if (tasks.isEmpty()) return "No active tasks found.";
            return tasks.stream()
                .map(t -> "[%s] [%s] [%s] %s".formatted(t.getId(), t.getPriority(), t.getStatus(), t.getTitle()))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list tasks: " + e.getMessage();
        }
    }

    public String completeTask(CompleteTaskRequest req) {
        try {
            UUID id = UUID.fromString(req.taskId());
            return taskItemRepository.findById(id).map(task -> {
                task.setStatus(TaskItem.Status.COMPLETED);
                taskItemRepository.save(task);
                return "Task '" + task.getTitle() + "' marked as completed.";
            }).orElse("No task found with ID: " + req.taskId());
        } catch (Exception e) {
            return "Failed to complete task: " + e.getMessage();
        }
    }

    public String deleteTask(DeleteTaskRequest req) {
        try {
            UUID id = UUID.fromString(req.taskId());
            if (!taskItemRepository.existsById(id)) return "No task found with ID: " + req.taskId();
            taskItemRepository.deleteById(id);
            return "Task " + req.taskId() + " deleted.";
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
                .withDescription("Lists all pending and in-progress tasks for the user.")
                .withInputType(ListTasksRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::completeTask)
                .withName("completeTask")
                .withDescription("Marks a task as completed by its UUID.")
                .withInputType(CompleteTaskRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::deleteTask)
                .withName("deleteTask")
                .withDescription("Deletes a task by its UUID.")
                .withInputType(DeleteTaskRequest.class)
                .build()
        );
    }
}
