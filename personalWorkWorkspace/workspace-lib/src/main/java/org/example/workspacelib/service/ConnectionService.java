package org.example.workspacelib.service;

import lombok.AllArgsConstructor;
import org.example.workspacelib.dto.create.ConnectionDto;
import org.example.workspacelib.entity.TodoConnectionEntity;
import org.example.workspacelib.entity.TodoEntity;
import org.example.workspacelib.repository.ConnectionRepository;
import org.example.workspacelib.repository.TodoRepository; // Предполагается, что есть репозиторий для TodoEntity
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final TodoRepository todoRepository; // Репозиторий для получения TodoEntity

    public void createConnection(ConnectionDto connectionDto) {
        // Получение объектов TodoEntity по их идентификаторам
        TodoEntity sourceTodo = todoRepository.findById(connectionDto.getSourceId())
                .orElseThrow(() -> new RuntimeException("Source Todo not found"));
        TodoEntity targetTodo = todoRepository.findById(connectionDto.getTargetId())
                .orElseThrow(() -> new RuntimeException("Target Todo not found"));

        // Логика сохранения соединения в базе данных
        connectionRepository.save(new TodoConnectionEntity(sourceTodo, targetTodo));
    }

    public void deleteConnection(ConnectionDto connectionDto) {
        // Получение объектов TodoEntity по их идентификаторам
        TodoEntity sourceTodo = todoRepository.findById(connectionDto.getSourceId())
                .orElseThrow(() -> new RuntimeException("Source Todo not found"));
        TodoEntity targetTodo = todoRepository.findById(connectionDto.getTargetId())
                .orElseThrow(() -> new RuntimeException("Target Todo not found"));

        // Удаление соединения
        connectionRepository.deleteByTodo1AndTodo2(sourceTodo, targetTodo);
    }

}
