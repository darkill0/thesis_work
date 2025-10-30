package org.example.workspacelib.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "todo_connections")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(TodoConnectionId.class)
public class TodoConnectionEntity {

    @Id
    @Column(name = "todo_id1")
    private Long todo1Id;

    @Id
    @Column(name = "todo_id2")
    private Long todo2Id;

    // Поля для связи с TodoEntity
    @ManyToOne
    @JoinColumn(name = "todo_id1", insertable = false, updatable = false)
    private TodoEntity todo1;

    @ManyToOne
    @JoinColumn(name = "todo_id2", insertable = false, updatable = false)
    private TodoEntity todo2;

    public TodoConnectionEntity(TodoEntity todo1, TodoEntity todo2) {
        this.todo1 = todo1;
        this.todo2 = todo2;
        this.todo1Id = todo1.getId();
        this.todo2Id = todo2.getId();
    }
}
