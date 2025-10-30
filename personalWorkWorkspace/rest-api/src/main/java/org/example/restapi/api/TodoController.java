package org.example.restapi.api;

import lombok.AllArgsConstructor;
import org.example.workspacelib.dto.create.AddTodoPosition;
import org.example.workspacelib.dto.create.ConnectionDto;
import org.example.workspacelib.dto.create.TodoCreateDto;
import org.example.workspacelib.dto.form.TodoForm;
import org.example.workspacelib.service.ConnectionService;
import org.example.workspacelib.service.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@AllArgsConstructor
public class TodoController {

    private final ConnectionService connectionService;


    private final TodoService todoService;
    public static final String createTodo = "/user/{id}/create/";
    public static final String getTodosFromProjectPath = "/user/{id}/project/{projectId}";
    public static final String getTodosFromTaskPath = "/user/{id}/task/{taskId}";
    public static final String getTodosFromTicketPath = "/user/{id}/ticket/{ticketId}";
    public static final String getTodosPath = "/user/{id}";
    public static final String addTodoPosition = "/todo/{id}";



    @PostMapping(createTodo)
    public ResponseEntity<TodoForm> createTodo(@PathVariable Long id, @RequestBody TodoCreateDto todo) {
        try {
            TodoForm form = todoService.createTodo(todo, id);
            if(form != null) {
                return ResponseEntity.ok(form);
            }
            return ResponseEntity.badRequest().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(getTodosPath)
    public ResponseEntity<List<TodoForm>> getTodos(@PathVariable Long id) {
        try{
            return ResponseEntity.ok(todoService.getGlobalTodos(id));
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(getTodosFromProjectPath)
    public ResponseEntity<List<TodoForm>> getTodosFromProject(@PathVariable Long id, @PathVariable Long projectId ) {
        try{
            return ResponseEntity.ok(todoService.getTodosFromProject(id, projectId));
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(getTodosFromTaskPath)
    public ResponseEntity<List<TodoForm>> getTodosFromTask(@PathVariable Long id, @PathVariable Long taskId ) {
        try{
            return ResponseEntity.ok(todoService.getTodosFromTask(id, taskId));
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(getTodosFromTicketPath)
    public ResponseEntity<List<TodoForm>> getTodosFromTicket(@PathVariable Long id, @PathVariable Long ticketId ) {
        try{
            return ResponseEntity.ok(todoService.getTodosFromTicket(id, ticketId));
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(addTodoPosition)
    public ResponseEntity<TodoForm> addTodoPosition(@PathVariable Long id, @RequestBody AddTodoPosition todoPost) {
        try{
            return ResponseEntity.ok(todoService.addTodoPosition(id, todoPost));
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/connections")
    public ResponseEntity<?> createConnection(@RequestBody ConnectionDto connectionDto) {
        System.out.println(connectionDto.toString());
        try {
            connectionService.createConnection(connectionDto);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/connections")
    public ResponseEntity<?> deleteConnection(@RequestBody ConnectionDto connectionDto) {
        connectionService.deleteConnection(connectionDto);
        return ResponseEntity.ok().build();
    }
}
