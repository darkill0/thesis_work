package org.example.workspacelib.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.entity.RoleEntity;
import org.example.workspacelib.entity.TaskEntity;
import org.example.workspacelib.entity.TicketEntity;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
    Long userId;
    String login;
    String passwordUser;
    Long roleId;
    Date createdAt;
    Date updatedAt;
    List<Project> projects;
    List<Task> tasksCreatedByUser;
    List<Task> acceptedTasksByUser;
    List<Ticket> ticketsCreatedByUser;
}
