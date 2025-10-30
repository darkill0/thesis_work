package org.example.workspacelib.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workspacelib.dto.create.AddTodoPosition;
import org.example.workspacelib.dto.create.TodoCreateDto;
import org.example.workspacelib.dto.form.TodoForm;
import org.example.workspacelib.entity.TodoEntity;
import org.example.workspacelib.entity.UserEntity;
import org.example.workspacelib.factory.FactoryTodo;
import org.example.workspacelib.repository.TodoRepository;
import org.example.workspacelib.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userService;

    public TodoForm createTodo(TodoCreateDto todoForm, Long id) {
        try{
            UserEntity user = userService.findById(id).get();

            TodoEntity todo = FactoryTodo.getTodoEntity(todoForm);
            todo.setUserId(user);
            return FactoryTodo.getTodoForm(todoRepository.saveAndFlush(todo));

        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    public List<TodoForm> getGlobalTodos(Long userId) {
        return  todoRepository.findByUserId(userService.findById(userId).get()).stream()
                .filter(todo -> todo.getTicketId() == null && todo.getProjectId() == null && todo.getTaskId() == null)
                .map(FactoryTodo::getTodoForm).toList();
    }

    public List<TodoForm> getTodosFromProject(Long userId, Long projectId) {
        return todoRepository.findByUserIdAndProjectId(userService.findById(userId).get(), projectId).stream().
                map(FactoryTodo::getTodoForm).toList();
    }

    public List<TodoForm> getTodosFromTask(Long userId, Long taskId) {
        return todoRepository.findByUserIdAndTaskId(userService.findById(userId).get(), taskId).stream().
                map(FactoryTodo::getTodoForm).toList();
    }

    public List<TodoForm> getTodosFromTicket(Long userId, Long ticketId) {
        return todoRepository.findByUserIdAndTicketId(userService.findById(userId).get(), ticketId).stream().
                map(FactoryTodo::getTodoForm).toList();
    }

    public TodoForm addTodoPosition(Long id, AddTodoPosition addTodoPosition){
        TodoEntity todo = todoRepository.findById(id).get();
        todo.setX(addTodoPosition.getX());
        todo.setY(addTodoPosition.getY());
       return FactoryTodo.getTodoForm( todoRepository.saveAndFlush(todo));
    }

}
