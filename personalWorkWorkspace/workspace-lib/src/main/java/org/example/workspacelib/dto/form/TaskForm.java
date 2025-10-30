package org.example.workspacelib.dto.form;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class TaskForm {
    Long taskId;
    String titleTask;
    String descriptionTask;
    Boolean status;
    Long project;
    Date createdAt;
    Date updatedAt;
    Long createdByUser;
    Long acceptedForExecutionByUser;
    List<TaskCommentForm> comments; // Список комментариев
}