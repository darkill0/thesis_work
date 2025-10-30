package org.example.workspacelib.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "todo")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    UserEntity userId;
    @Column(name = "project_id")
    Long projectId;
    @Column(name = "task_id")
    Long taskId;
    @Column(name = "ticket_id")
    Long ticketId;
    @Column(name = "description")
    String description;
    @Column(name = "color")
    String color;
    @Column(name = "status")
    Boolean status;
    @Column(name = "created_at")
    Date createdAt;
    @Column(name = "updated_at")
    Date updatedAt;


    @Column(name = "x")
    Long x;

    @Column(name = "y")
    Long y;

    @ManyToMany
    @JoinTable(
            name = "todo_connections",
            joinColumns = @JoinColumn(name = "todo_id1"),
            inverseJoinColumns = @JoinColumn(name = "todo_id2")
    )
    Set<TodoEntity> connectedTodos;
}
