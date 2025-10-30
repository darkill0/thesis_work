package org.example.restapi.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.components.kafka.EventsNotification;
import org.example.workspacelib.dto.create.TaskCommentDto;
import org.example.workspacelib.dto.create.TaskCreateDto;
import org.example.workspacelib.dto.form.TaskCommentForm;
import org.example.workspacelib.dto.form.TaskForm;
import org.example.workspacelib.dto.form.TasklAssignDto;
import org.example.workspacelib.dto.form.UserForm;
import org.example.workspacelib.dto.update.TaskUpdateDto;
import org.example.workspacelib.kafkacon.KafkaWorkWithBroker;
import org.example.workspacelib.service.FollowerToProjectService;
import org.example.workspacelib.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "API контроллер для задач проекта")
@RestController
@RequestMapping("api/task")
@Controller
public class TaskController {

    private final String task_create = "create";
    private final String task_delete = "delete/{id}";
    private final String task_update = "update/{id}";
    private final String task_complete = "complete/{id}";
    private final String task_get = "get/{id}";
    private final String task_list = "list";
    private final String task_list_by = "list/by/{id}";
    private final String task_assign = "assign/{id}";
    private final String task_comment_add = "comment/{taskId}/add";

    private final TaskService taskService;
    private final FollowerToProjectService followerToProjectService;
    private final KafkaWorkWithBroker kafkaWorkWithBroker;

    @Autowired
    public TaskController(TaskService taskService, FollowerToProjectService followerToProjectService, KafkaWorkWithBroker kafkaWorkWithBroker) {
        this.taskService = taskService;
        this.followerToProjectService = followerToProjectService;
        this.kafkaWorkWithBroker = kafkaWorkWithBroker;
    }

    @Operation(summary = "Создание задачи", description = "Эндпоинт для создания новой задачи")
    @PostMapping(task_create)
    public ResponseEntity<TaskForm> createTask(@RequestBody TaskCreateDto task) {
        try {
            TaskForm taskForm = taskService.createTask(task);
            List<UserForm> followers = followerToProjectService.getFollowerToProjects(taskForm.getProject());
            for (UserForm user : followers) {
                kafkaWorkWithBroker.sendMessage(
                        "Создана задача [" + taskForm.toString() + "]",
                        EventsNotification.CREATE_TASK,
                        Math.toIntExact(task.getCreatedByUser()),
                        Math.toIntExact(user.getUserId())
                );
            }
            return ResponseEntity.ok(taskForm);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Удаление задачи", description = "Эндпоинт для удаления задачи")
    @DeleteMapping(task_delete)
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            TaskForm taskForm = taskService.getTask(id);
            taskService.deleteTask(id);
            kafkaWorkWithBroker.sendMessage(
                    "Удалена задача с id " + id,
                    EventsNotification.CLOSE_TASK,
                    Math.toIntExact(taskForm.getCreatedByUser()),
                    Math.toIntExact(taskForm.getAcceptedForExecutionByUser())
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Обновление задачи", description = "Эндпоинт для обновления задачи")
    @PostMapping(task_update)
    public ResponseEntity<TaskForm> updateTask(@PathVariable Long id, @RequestBody TaskUpdateDto task) {
        try {
            TaskForm taskForm = taskService.updateTask(id, task);
            kafkaWorkWithBroker.sendMessage(
                    "Обновлена задача [" + taskForm.toString() + "]",
                    EventsNotification.UPDATE_TASK,
                    Math.toIntExact(taskForm.getCreatedByUser()),
                    Math.toIntExact(taskForm.getAcceptedForExecutionByUser())
            );
            return ResponseEntity.ok(taskForm);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Завершение задачи", description = "Эндпоинт для завершения задачи")
    @PostMapping(task_complete)
    public ResponseEntity<TaskForm> completeTask(@PathVariable Long id) {
        try {
            TaskForm taskForm = taskService.completeTask(id);
            kafkaWorkWithBroker.sendMessage(
                    "Завершена задача [" + taskForm.toString() + "]",
                    EventsNotification.COMPLETE_TASK,
                    Math.toIntExact(taskForm.getCreatedByUser()),
                    Math.toIntExact(taskForm.getAcceptedForExecutionByUser())
            );
            return ResponseEntity.ok(taskForm);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Взять задачу в обработку", description = "Эндпоинт для взятия задачи в обработку")
    @PostMapping(task_assign)
    public ResponseEntity<TaskForm> assignTask(@PathVariable Long id, @RequestBody TasklAssignDto task) {
        try {
            TaskForm taskForm = taskService.assign(id, task);
            kafkaWorkWithBroker.sendMessage(
                    "Взята задача в обработку [" + taskForm.toString() + "]",
                    EventsNotification.UPDATE_TASK,
                    Math.toIntExact(taskForm.getAcceptedForExecutionByUser()),
                    Math.toIntExact(taskForm.getCreatedByUser())
            );
            return ResponseEntity.ok(taskForm);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Добавление комментария к задаче", description = "Эндпоинт для добавления комментария к задаче")
    @PostMapping(task_comment_add)
    public ResponseEntity<TaskCommentForm> addComment(@PathVariable Long taskId, @RequestBody TaskCommentDto commentDto) {
        try {
            TaskCommentForm comment = taskService.addComment(taskId, commentDto);
            kafkaWorkWithBroker.sendMessage(
                    "Добавлен комментарий к задаче [" + taskId + "]: " + comment.getDescription(),
                    EventsNotification.COMMENT_ADDED,
                    Math.toIntExact(comment.getCreatedByUser()),
                    Math.toIntExact(taskService.getTask(taskId).getCreatedByUser())
            );
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение задачи по id", description = "Эндпоинт для получения задачи")
    @GetMapping(task_get)
    public ResponseEntity<TaskForm> getTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.getTask(id));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение задач", description = "Эндпоинт для получения всех задач")
    @GetMapping(task_list)
    public ResponseEntity<List<TaskForm>> getAllTask() {
        try {
            return ResponseEntity.ok(taskService.getTasks());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение задач пользователей", description = "Эндпоинт для получения задач пользователя")
    @GetMapping(task_list_by)
    public ResponseEntity<List<TaskForm>> getAllTaskBy(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.getTasksByUser(id));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}