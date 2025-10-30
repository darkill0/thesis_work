package org.example.workspacelib.repository;

import org.example.workspacelib.entity.TodoConnectionEntity;
import org.example.workspacelib.entity.TodoConnectionId;
import org.example.workspacelib.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<TodoConnectionEntity, Long> {
    void deleteByTodo1AndTodo2(TodoEntity todo1, TodoEntity todo2);
}
