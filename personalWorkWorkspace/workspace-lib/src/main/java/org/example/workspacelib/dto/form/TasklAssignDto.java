package org.example.workspacelib.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TasklAssignDto {
    Long  acceptedForExecutionByUser;
}
