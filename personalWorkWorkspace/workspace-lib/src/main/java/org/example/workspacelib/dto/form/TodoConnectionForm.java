package org.example.workspacelib.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoConnectionForm {
    private Long todoId1;  // Идентификатор первой задачи
    private Long todoId2;  // Идентификатор второй задачи
}