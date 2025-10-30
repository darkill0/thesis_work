package org.example.workspacelib.models;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long commentId;
    private String description;
    private int status;
    private Date createdAt;
    private Long createdByUser; // Long вместо UserEntity
}