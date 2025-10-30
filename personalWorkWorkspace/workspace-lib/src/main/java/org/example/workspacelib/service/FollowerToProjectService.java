package org.example.workspacelib.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.workspacelib.dto.form.ProjectForm;
import org.example.workspacelib.dto.form.UserForm;
import org.example.workspacelib.entity.FollowerToProjectEntity;
import org.example.workspacelib.factory.FactoryProject;
import org.example.workspacelib.factory.FactoryUser;
import org.example.workspacelib.repository.FollowerToProjectEntityRepository;
import org.example.workspacelib.repository.ProjectRepository;
import org.example.workspacelib.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor

public class FollowerToProjectService {

    private final FollowerToProjectEntityRepository followerToProjectEntityRepository;
    private final UserRepository userService;
    private final ProjectRepository projectService;

    public Boolean followProject(Long follower, Long project) {
        FollowerToProjectEntity followerToProjectEntity =
                FollowerToProjectEntity.builder()
                        .follower(userService.findById(follower).get())
                        .project(projectService.findById(project).get())
                        .build();
        try {
            followerToProjectEntityRepository.saveAndFlush(followerToProjectEntity);
            return true;

        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Boolean unfollowProject(Long follower, Long project) {
        FollowerToProjectEntity f = followerToProjectEntityRepository.findByFollowerAndProject(userService.findById(follower).get(), projectService.findById(project).get());
        if(f != null){
            followerToProjectEntityRepository.delete(f);
            return true;
        }
        return false;
    }

    public List<ProjectForm> getProjectsToUser (Long userId) {
        List<FollowerToProjectEntity> f = followerToProjectEntityRepository.findAll().stream().filter(u -> u.getFollower().equals(userService.findById(userId).get())).toList();
        List<ProjectForm> projectForms = new ArrayList<>();
        for(FollowerToProjectEntity fp : f){
            projectForms.add(FactoryProject.ProjectDtoForm(fp.getProject()));
        }
        return projectForms;
    }

    public List<UserForm> getFollowerToProjects(Long projectId) {
        List<FollowerToProjectEntity> f = followerToProjectEntityRepository.findAll().stream().filter(u -> u.getFollower().equals(projectService.findById(projectId).get())).toList();
        List<UserForm> userForms = new ArrayList<>();
        for(FollowerToProjectEntity fp : f){
            userForms.add(FactoryUser.GetUserForm(fp.getFollower()));
        }
        return userForms;
    }
}
