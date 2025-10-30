package org.example.workspacelib.models;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.example.workspacelib.entity.CommentToTask;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.entity.UserEntity;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Task {
    Long taskId;
    String titleTask;
    String descriptionTask;
    Boolean status;
    ProjectEntity project;
    Date createdAt;
    Date updatedAt;
    UserEntity createdByUser;
    UserEntity acceptedForExecutionByUser;
    List<CommentToTask> comments;
}