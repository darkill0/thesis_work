package org.example.workspacelib.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskCreateDto {
    String titleTask;
    String descriptionTask;
    Long project;
    Long createdByUser;
}
