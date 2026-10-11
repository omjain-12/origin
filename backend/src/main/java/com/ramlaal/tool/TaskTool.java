package com.ramlaal.tool;

import com.ramlaal.domain.entity.TaskItem;
import com.ramlaal.domain.repository.TaskItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskTool {

    private final TaskItemRepository taskItemRepository;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    @Tool(description = "Creates a new task/todo item. Returns the created task's ID.")
    public String createTask(
        @ToolParam(description = "Task title") String title,
        @ToolParam(description = "Optional description") String description,
        @ToolParam(description = "Priority: LOW, MEDIUM, HIGH, or URGENT") String priority
    ) {
        try {
            TaskItem task = new TaskItem();
            task.setUserId(userId);
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(TaskItem.Priority.valueOf(priority.toUpperCase()));
            TaskItem saved = taskItemRepository.save(task);
            return "Task created with ID: " + saved.getId() + " — " + saved.getTitle();
        } catch (Exception e) {
            return "Failed to create task: " + e.getMessage();
        }
    }

    @Tool(description = "Lists all pending and in-progress tasks for the user.")
    public String listTasks() {
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

    @Tool(description = "Marks a task as completed by its ID.")
    public String completeTask(
        @ToolParam(description = "UUID of the task to complete") String taskId
    ) {
        try {
            UUID id = UUID.fromString(taskId);
            return taskItemRepository.findById(id).map(task -> {
                task.setStatus(TaskItem.Status.COMPLETED);
                taskItemRepository.save(task);
                return "Task '" + task.getTitle() + "' marked as completed.";
            }).orElse("No task found with ID: " + taskId);
        } catch (Exception e) {
            return "Failed to complete task: " + e.getMessage();
        }
    }

    @Tool(description = "Deletes a task by its ID.")
    public String deleteTask(
        @ToolParam(description = "UUID of the task to delete") String taskId
    ) {
        try {
            UUID id = UUID.fromString(taskId);
            if (!taskItemRepository.existsById(id)) return "No task found with ID: " + taskId;
            taskItemRepository.deleteById(id);
            return "Task " + taskId + " deleted.";
        } catch (Exception e) {
            return "Failed to delete task: " + e.getMessage();
        }
    }
}
