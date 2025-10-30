package org.example.workspacelib.repository;

import org.example.workspacelib.entity.TodoEntity;
import org.example.workspacelib.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
    List<TodoEntity> findByUserId(UserEntity userId);
    List<TodoEntity> findByUserIdAndProjectId(UserEntity userId, Long projectId);
    List<TodoEntity> findByUserIdAndTaskId(UserEntity userId, Long taskId);
    List<TodoEntity> findByUserIdAndTicketId(UserEntity userId, Long ticketId);
}
