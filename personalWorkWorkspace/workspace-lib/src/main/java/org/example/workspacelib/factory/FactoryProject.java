package org.example.workspacelib.factory;

import org.example.workspacelib.dto.create.ProjectCreateDto;
import org.example.workspacelib.dto.form.ProjectForm;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.models.Project;

import java.util.Date;

public class FactoryProject {



    public static Project GetProject(ProjectEntity projectEntity) {
        return Project.builder().projectId(projectEntity.getProjectId())
                .urlToProject(projectEntity.getUrlToProject())
                .title(projectEntity.getTitle())
                .descrption(projectEntity.getDescrption())
                .createdAt(projectEntity.getCreatedAt())
                .updatedAt(projectEntity.getUpdatedAt())
                .createdByUser(projectEntity.getCreatedByUser().getUserId())
                .build();
    }

    public static ProjectEntity ProjectDtoConvert(ProjectCreateDto projectCreateDto){
        return ProjectEntity.builder()
                .title(projectCreateDto.getTitle())
                .descrption(projectCreateDto.getDescrption())
                .urlToProject(projectCreateDto.getUrlToProject())
                .updatedAt(new Date())
                .createdAt(new Date())
                .build();
    }

    public static ProjectForm ProjectDtoForm(ProjectEntity projectEntity){
        return ProjectForm.builder()
                .projectId(projectEntity.getProjectId())
                .tasks(projectEntity.getTasks().stream().map(FactoryTask::GetTaskForm).toList())
                .urlToProject(projectEntity.getUrlToProject())
                .title(projectEntity.getTitle())
                .createdAt(projectEntity.getCreatedAt())
                .updatedAt(projectEntity.getUpdatedAt())
                .descrption(projectEntity.getDescrption())
                .createdByUser(projectEntity.getCreatedByUser().getUserId())
                .build();
    }

    public static ProjectEntity GetProjectEntity(Project project) {
        return ProjectEntity.builder().projectId(project.getProjectId())
                .urlToProject(project.getUrlToProject())
                .title(project.getTitle())
                .descrption(project.getDescrption())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
