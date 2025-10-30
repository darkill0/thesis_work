package org.example.workspacelib.dto.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskUpdateDto {
    String titleTask;
    String descriptionTask;
    Boolean status;
    Long project;
    Long  acceptedForExecutionByUser;
}
