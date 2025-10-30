package org.example.workspacelib.dto.form;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.example.workspacelib.entity.UserEntity;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoForm {
    Long id;
    Long projectId;
    Long taskId;
    Long ticketId;
    String description;
    String color;
    Boolean status;
    Date createdAt;
    Date updatedAt;
    Long x;
    Long y;

    Set<Long> connectedTodoIds;  // Множество идентификаторов соединенных задач


}
