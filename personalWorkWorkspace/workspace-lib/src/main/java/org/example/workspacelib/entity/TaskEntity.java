package org.example.workspacelib.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "task")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    Long taskId;

    @Column(name = "titleTask")
    String titleTask;

    @Column(name = "descriptionTask")
    String descriptionTask;

    @Column(name = "status")
    Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    ProjectEntity project;

    @Column(name = "createdAt")
    Date createdAt;

    @Column(name = "updatedAt")
    Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdByUser", referencedColumnName = "userId")
    UserEntity createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptedForExecutionByUser", referencedColumnName = "userId")
    UserEntity acceptedForExecutionByUser;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    List<CommentToTask> commentsToTask;
}