package org.example.workspacelib.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "followers_to_project")
@IdClass(FollowerToProjectId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowerToProjectEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", referencedColumnName = "userId", nullable = false)
    UserEntity follower;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    ProjectEntity project;

    @Column(name = "subscribed_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    Date subscribedAt;
}
