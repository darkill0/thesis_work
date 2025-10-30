package org.example.workspacelib.entity;

import java.io.Serializable;

public class TodoConnectionId implements Serializable {
    private Long todo1Id;
    private Long todo2Id;

    // Пустой конструктор
    public TodoConnectionId() {}

    public TodoConnectionId(Long todo1Id, Long todo2Id) {
        this.todo1Id = todo1Id;
        this.todo2Id = todo2Id;
    }

    // equals() и hashCode() методы

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodoConnectionId)) return false;

        TodoConnectionId that = (TodoConnectionId) o;

        if (!todo1Id.equals(that.todo1Id)) return false;
        return todo2Id.equals(that.todo2Id);
    }

    @Override
    public int hashCode() {
        int result = todo1Id.hashCode();
        result = 31 * result + todo2Id.hashCode();
        return result;
    }
}
