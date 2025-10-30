package org.example.workspacelib.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketCreateDto {
    private String descrption;
    private String urlToProject;
    private Long createdByUser;
}