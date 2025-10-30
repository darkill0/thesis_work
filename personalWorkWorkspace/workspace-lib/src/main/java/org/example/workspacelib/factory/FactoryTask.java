package org.example.workspacelib.factory;

import org.example.workspacelib.dto.form.TaskCommentForm;
import org.example.workspacelib.dto.form.TaskForm;
import org.example.workspacelib.entity.CommentToTask;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.entity.TaskEntity;
import org.example.workspacelib.entity.UserEntity;
import org.example.workspacelib.models.Task;

import java.util.List;
import java.util.stream.Collectors;

public class FactoryTask {

    public static Task GetTask(TaskEntity taskEntity) {
        return Task.builder()
                .taskId(taskEntity.getTaskId())
                .titleTask(taskEntity.getTitleTask())
                .descriptionTask(taskEntity.getDescriptionTask())
                .status(taskEntity.getStatus())
                .project(taskEntity.getProject())
                .createdAt(taskEntity.getCreatedAt())
                .updatedAt(taskEntity.getUpdatedAt())
                .createdByUser(taskEntity.getCreatedByUser())
                .acceptedForExecutionByUser(taskEntity.getAcceptedForExecutionByUser())
                .comments(taskEntity.getCommentsToTask())
                .build();
    }

    public static TaskEntity GetTaskEntity(Task task) {
        return TaskEntity.builder()
                .taskId(task.getTaskId())
                .titleTask(task.getTitleTask())
                .descriptionTask(task.getDescriptionTask())
                .status(task.getStatus())
                .project(task.getProject())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdByUser(task.getCreatedByUser())
                .acceptedForExecutionByUser(task.getAcceptedForExecutionByUser())
                .commentsToTask(task.getComments())
                .build();
    }

    public static TaskForm GetTaskForm(TaskEntity taskEntity) {
        List<TaskCommentForm> comments = taskEntity.getCommentsToTask() != null
                ? taskEntity.getCommentsToTask().stream()
                .map(comment -> TaskCommentForm.builder()
                        .commentId((long) comment.getComment_id())
                        .description(comment.getDescription())
                        .createdByUser(taskEntity.getCreatedByUser().getUserId())
                        .createdAt(comment.getCreated_at())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return TaskForm.builder()
                .taskId(taskEntity.getTaskId())
                .titleTask(taskEntity.getTitleTask())
                .descriptionTask(taskEntity.getDescriptionTask())
                .status(taskEntity.getStatus())
                .project(taskEntity.getProject().getProjectId())
                .createdAt(taskEntity.getCreatedAt())
                .updatedAt(taskEntity.getUpdatedAt())
                .createdByUser(taskEntity.getCreatedByUser().getUserId())
                .acceptedForExecutionByUser(
                        taskEntity.getAcceptedForExecutionByUser() != null
                                ? taskEntity.getAcceptedForExecutionByUser().getUserId()
                                : null)
                .comments(comments)
                .build();
    }
}