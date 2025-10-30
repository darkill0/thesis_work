package org.example.workspacelib.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectCreateDto {
    String title;
    String descrption;
    String urlToProject;
    Long createdByUser;

}
