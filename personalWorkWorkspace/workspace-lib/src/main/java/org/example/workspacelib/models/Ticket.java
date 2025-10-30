package org.example.workspacelib.models;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private Long ticketId;
    private String descrption;
    private String urlToProject;
    private Long createdByUser; // Long вместо UserEntity
    private Long acceptedByUser; // Long вместо UserEntity
    private Date createdAt;
    private Date updatedAt;
    private int status;
    private List<Comment> comments; // List<Comment> из models
}