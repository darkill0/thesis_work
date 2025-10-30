package org.example.workspacelib.dto.form;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketForm {
    private Long ticketId;
    private String descrption;
    private String urlToProject;
    private Long createdByUser;
    private Long acceptedByUser;
    private Date createdAt;
    private Date updatedAt;
    private int status;
    private List<CommentForm> comments;
}