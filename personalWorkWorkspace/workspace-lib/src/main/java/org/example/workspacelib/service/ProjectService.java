package org.example.workspacelib.service;

import org.example.workspacelib.dto.create.ProjectCreateDto;
import org.example.workspacelib.dto.form.ProjectForm;
import org.example.workspacelib.dto.update.ProjectUpdateDto;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.factory.FactoryProject;
import org.example.workspacelib.models.Project;
import org.example.workspacelib.repository.ProjectRepository;
import org.example.workspacelib.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ProjectService {
    ProjectRepository projectRepository;
    UserRepository userRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(ProjectCreateDto project) {
        if (project.getCreatedByUser() == null) {
            System.out.println(project.toString());
            return null;
        }
        ProjectEntity projectEntity =   FactoryProject.ProjectDtoConvert(project);
        projectEntity.setCreatedByUser(userRepository.findById(project.getCreatedByUser()).get());
        projectRepository.saveAndFlush(projectEntity);
        return FactoryProject.GetProject(projectEntity);

    }

    public ProjectForm getProject(Long id) {
        return projectRepository.findById(id).map(FactoryProject::ProjectDtoForm).orElse(null);
    }

    public List<ProjectForm> getAllProjects() {
        return projectRepository.findAll().stream().map(FactoryProject::ProjectDtoForm).toList();
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public ProjectForm updateProject(Long id, ProjectUpdateDto project) {

        ProjectEntity projectEntity = projectRepository.findById(id).orElse(null);
        if(projectEntity != null) {
            projectEntity.setUrlToProject(project.getUrlToProject());
            projectEntity.setTitle(project.getTitle());
            projectEntity.setDescrption(project.getDescrption());
            projectEntity.setUpdatedAt(new Date());
            projectRepository.saveAndFlush(projectEntity);
            return getProject(id);

        }
        return null;
    }

    public List<ProjectForm> getAllProjectsBy(Long id) {
        return projectRepository.findAll().stream().filter(p -> p.getCreatedByUser().getUserId().equals(id)).map(FactoryProject::ProjectDtoForm).toList();
    }
}
