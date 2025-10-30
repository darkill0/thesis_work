package org.example.workspacelib.dto.create;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TodoCreateDto {
    Long projectId;
    Long taskId;
    Long ticketId;
    String description;
    String color;
}
