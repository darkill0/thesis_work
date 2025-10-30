package org.example.workspacelib.factory;

import org.example.workspacelib.dto.create.TodoCreateDto;
import org.example.workspacelib.dto.form.TodoForm;
import org.example.workspacelib.entity.TodoEntity;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class FactoryTodo {
    public  static TodoForm getTodoForm(TodoEntity todo){
        return TodoForm.builder()
                .id(todo.getId())
                .taskId(todo.getTaskId())
                .color(todo.getColor())
                .status(todo.getStatus())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .description(todo.getDescription())
                .projectId(todo.getProjectId())
                .x(todo.getX())
                .y(todo.getY())
                .connectedTodoIds(getConnectedTodoIds(todo)) // Получение идентификаторов соединённых задач
                .build();
    }

    private static Set<Long> getConnectedTodoIds(TodoEntity todo) {
        if(todo.getConnectedTodos() != null){
            return todo.getConnectedTodos().stream()
                    .map(connectedTodo -> connectedTodo.getId()) // Получение идентификатора соединённой задачи
                    .collect(Collectors.toSet()); // Сбор в Set для устранения дублирования
        }
        return null;

    }

    public  static TodoEntity getTodoEntity(TodoCreateDto todoForm){
        return TodoEntity.builder()
                .taskId(todoForm.getTaskId())
                .projectId(todoForm.getProjectId())
                .ticketId(todoForm.getTicketId())
                .color(todoForm.getColor())
                .description(todoForm.getDescription())
                .createdAt(new Date())
                .updatedAt(new Date())
                .status(false)
                .x(null)
                .y(null)
                .build();
    }


}
