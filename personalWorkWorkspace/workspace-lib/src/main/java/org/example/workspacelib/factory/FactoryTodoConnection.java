package org.example.workspacelib.factory;

import org.example.workspacelib.dto.form.TodoConnectionForm;
import org.example.workspacelib.entity.TodoConnectionEntity;

public class FactoryTodoConnection {

    public static TodoConnectionForm getTodoConnectionForm(TodoConnectionEntity todos) {
        return TodoConnectionForm.builder()
                .todoId1(todos.getTodo1().getId())
                .todoId2(todos.getTodo2().getId())
                .build();
    }
}
