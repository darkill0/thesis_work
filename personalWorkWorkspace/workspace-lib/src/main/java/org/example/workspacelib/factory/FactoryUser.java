package org.example.workspacelib.factory;

import org.example.workspacelib.dto.create.UserCreateDto;
import org.example.workspacelib.dto.form.UserForm;
import org.example.workspacelib.entity.UserEntity;
import org.example.workspacelib.models.User;

import java.util.Optional;

public class FactoryUser {
    public static User GetUser(UserEntity userEntity) {
        return User.builder()
                .userId(userEntity.getUserId())
                .passwordUser(userEntity.getPasswordUser())
                .login(userEntity.getLogin())
                .roleId(userEntity.getRoleId().getRoleId())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .projects(Optional.ofNullable(userEntity.getProjects())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryProject::GetProject).toList())
                        .orElse(null))
                .tasksCreatedByUser(Optional.ofNullable(userEntity.getTasksCreatedByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTask::GetTask).toList())
                        .orElse(null))
                .acceptedTasksByUser(Optional.ofNullable(userEntity.getAcceptedTasksByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTask::GetTask).toList())
                        .orElse(null))
                .ticketsCreatedByUser(Optional.ofNullable(userEntity.getTicketsCreatedByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTicket::GetTicket).toList())
                        .orElse(null))
                .build();
    }

    public static UserForm GetUserForm(UserEntity userEntity) {
        return UserForm.builder()
                .userId(userEntity.getUserId())
                .passwordUser(userEntity.getPasswordUser())
                .login(userEntity.getLogin())
                .roleId(userEntity.getRoleId().getRoleId())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .projects(Optional.ofNullable(userEntity.getProjects())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryProject::ProjectDtoForm).toList())
                        .orElse(null))
                .tasksCreatedByUser(Optional.ofNullable(userEntity.getTasksCreatedByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTask::GetTaskForm).toList())
                        .orElse(null))
                .acceptedTasksByUser(Optional.ofNullable(userEntity.getAcceptedTasksByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTask::GetTaskForm).toList())
                        .orElse(null))
                .ticketsCreatedByUser(Optional.ofNullable(userEntity.getTicketsCreatedByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTicket::GetTicketForm).toList())
                        .orElse(null))
                .url(userEntity.getUrl())
                .followedProjects(userEntity.getFollowedProjects() != null ? userEntity.getFollowedProjects().stream()
                        .map(p -> FactoryProject.GetProject(p.getProject()).getProjectId()).toList() : null)
                .build();
    }

    public static UserCreateDto GetUserDto(UserEntity userEntity) {
        return UserCreateDto.builder()
                .login(userEntity.getLogin())
                .passwordUser(userEntity.getPasswordUser())
                .roleId(userEntity.getRoleId().getRoleId())
                .build();
    }

    public static UserEntity GetUserEntity(User user) {
        return UserEntity.builder()
                .userId(user.getUserId())
                .passwordUser(user.getPasswordUser())
                .login(user.getLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .projects(Optional.ofNullable(user.getProjects())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryProject::GetProjectEntity).toList())
                        .orElse(null))
                .tasksCreatedByUser(Optional.ofNullable(user.getTasksCreatedByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTask::GetTaskEntity).toList())
                        .orElse(null))
                .acceptedTasksByUser(Optional.ofNullable(user.getAcceptedTasksByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTask::GetTaskEntity).toList())
                        .orElse(null))
                .ticketsCreatedByUser(Optional.ofNullable(user.getTicketsCreatedByUser())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.stream().map(FactoryTicket::GetTicketEntity).toList())
                        .orElse(null))
                .build();
    }
}