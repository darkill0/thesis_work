package org.example.workspacelib.dto.form;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentForm {
    private Long commentId;
    private String description;
    private int status;
    private Date createdAt;
    private Long createdByUser;
}