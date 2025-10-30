package org.example.workspacelib.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId") // Изменено на user_id
    private Long userId;

    @Column(name = "login")
    private String login;

    @Column(name = "password_user")
    private String passwordUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "roleId") // Исправлено на role_id
    private RoleEntity roleId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "url")
    private String url;

    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL)
    private List<ProjectEntity> projects;

    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL)
    private List<TaskEntity> tasksCreatedByUser;

    @OneToMany(mappedBy = "acceptedForExecutionByUser", cascade = CascadeType.ALL)
    private List<TaskEntity> acceptedTasksByUser;

    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL)
    private List<TicketEntity> ticketsCreatedByUser;

    @OneToMany(mappedBy = "acceptedByUser", cascade = CascadeType.ALL)
    private List<TicketEntity> ticketsAccepteddByUser;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    private List<TodoEntity> todos;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL)
    private List<FollowerToProjectEntity> followedProjects;
}