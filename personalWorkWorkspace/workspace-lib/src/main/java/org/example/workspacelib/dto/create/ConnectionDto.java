package org.example.workspacelib.dto.create;

import lombok.Data;

@Data
public class ConnectionDto {
    private Long sourceId;
    private Long targetId;
}
