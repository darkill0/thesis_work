package org.example.workspacelib.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")  // Исправлено имя колонки
    Long projectId;

    @Column(name = "title")
    String title;

    @Column(name = "descrption")
    String descrption;

    @Column(name = "url_to_project")
    String urlToProject;

    @Column(name = "created_at")
    Date createdAt;

    @Column(name = "updated_at")
    Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user", referencedColumnName = "userId")  // Исправлено имя колонки
    UserEntity createdByUser;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    List<TaskEntity> tasks;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    List<FollowerToProjectEntity> followers;

}
