package org.example.workspacelib.dto.update;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.workspacelib.dto.form.TaskForm;

import java.util.Date;
import java.util.List;
@Data
@Setter
@Getter
@Builder
public class ProjectUpdateDto {
    String title;
    String descrption;
    String urlToProject;

}
