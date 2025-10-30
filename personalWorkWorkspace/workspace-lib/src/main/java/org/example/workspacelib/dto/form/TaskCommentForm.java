package org.example.workspacelib.dto.form;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TaskCommentForm {
    private Long commentId;
    private String description;
    private Long createdByUser;
    private Date createdAt;
}