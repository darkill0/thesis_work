package org.example.workspacelib.dto.update;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Setter
@Getter
@Builder
public class TicketUpdateDto {
    String descrption;
    String urlToProject;
    int status;
}
