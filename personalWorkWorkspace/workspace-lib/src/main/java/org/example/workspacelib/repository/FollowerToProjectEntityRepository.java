package org.example.workspacelib.repository;

import org.example.workspacelib.entity.FollowerToProjectEntity;
import org.example.workspacelib.entity.FollowerToProjectId;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowerToProjectEntityRepository extends JpaRepository<FollowerToProjectEntity, FollowerToProjectId> {
    FollowerToProjectEntity findByFollowerAndProject(UserEntity follower, ProjectEntity project);
}
