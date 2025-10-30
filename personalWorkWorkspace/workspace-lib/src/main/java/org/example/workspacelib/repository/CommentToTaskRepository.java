package org.example.workspacelib.repository;

import org.example.workspacelib.entity.CommentToTask;
import org.example.workspacelib.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentToTaskRepository extends JpaRepository<CommentToTask, Integer> {
    @Query("SELECT c FROM CommentToTask c WHERE c.task = :task")
    List<CommentToTask> findByTask(@Param("task") TaskEntity task);
}