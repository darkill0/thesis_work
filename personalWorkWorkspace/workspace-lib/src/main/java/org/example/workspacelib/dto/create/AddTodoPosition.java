package org.example.workspacelib.dto.create;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddTodoPosition {
    Long x;
    Long y;

}
