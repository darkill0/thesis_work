package org.example.workspacelib.dto.form;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.entity.RoleEntity;
import org.example.workspacelib.entity.TaskEntity;
import org.example.workspacelib.entity.TicketEntity;

import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
@Builder
public class UserForm {

    Long userId;
    String login;
    String passwordUser;
    Long roleId;
    Date createdAt;
    Date updatedAt;
    List<ProjectForm> projects;
    List<TaskForm> tasksCreatedByUser;
    List<TaskForm> acceptedTasksByUser;
    List<TicketForm> ticketsCreatedByUser;
    String email;
    String url;
    List<Long> followedProjects;
}
