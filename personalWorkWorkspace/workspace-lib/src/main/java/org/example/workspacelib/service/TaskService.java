package org.example.workspacelib.service;

import org.example.workspacelib.dto.create.TaskCommentDto;
import org.example.workspacelib.dto.create.TaskCreateDto;
import org.example.workspacelib.dto.form.TaskCommentForm;
import org.example.workspacelib.dto.form.TaskForm;
import org.example.workspacelib.dto.form.TasklAssignDto;
import org.example.workspacelib.dto.update.TaskUpdateDto;
import org.example.workspacelib.entity.CommentToTask;
import org.example.workspacelib.entity.TaskEntity;
import org.example.workspacelib.factory.FactoryTask;
import org.example.workspacelib.repository.CommentToTaskRepository;
import org.example.workspacelib.repository.ProjectRepository;
import org.example.workspacelib.repository.TaskRepository;
import org.example.workspacelib.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentToTaskRepository commentToTaskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       UserRepository userRepository, CommentToTaskRepository commentToTaskRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.commentToTaskRepository = commentToTaskRepository;
    }

    public TaskForm getTask(Long id) {
        return taskRepository.findById(id)
                .map(taskEntity -> {
                    List<TaskCommentForm> comments = commentToTaskRepository.findByTask(taskEntity)
                            .stream()
                            .map(comment -> TaskCommentForm.builder()
                                    .commentId((long) comment.getComment_id())
                                    .description(comment.getDescription())
                                    .createdByUser(taskEntity.getCreatedByUser().getUserId())
                                    .createdAt(comment.getCreated_at())
                                    .build())
                            .collect(Collectors.toList());
                    TaskForm taskForm = FactoryTask.GetTaskForm(taskEntity);
                    taskForm.setComments(comments);
                    return taskForm;
                })
                .orElseThrow(() -> new IllegalArgumentException("Задача с ID " + id + " не найдена"));
    }

    public List<TaskForm> getTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskEntity -> {
                    List<TaskCommentForm> comments = commentToTaskRepository.findByTask(taskEntity)
                            .stream()
                            .map(comment -> TaskCommentForm.builder()
                                    .commentId((long) comment.getComment_id())
                                    .description(comment.getDescription())
                                    .createdByUser(taskEntity.getCreatedByUser().getUserId())
                                    .createdAt(comment.getCreated_at())
                                    .build())
                            .collect(Collectors.toList());
                    TaskForm taskForm = FactoryTask.GetTaskForm(taskEntity);
                    taskForm.setComments(comments);
                    return taskForm;
                })
                .collect(Collectors.toList());
    }

    public TaskForm createTask(TaskCreateDto task) {
        TaskEntity taskEntity = TaskEntity.builder()
                .titleTask(task.getTitleTask())
                .descriptionTask(task.getDescriptionTask())
                .project(projectRepository.findById(task.getProject())
                        .orElseThrow(() -> new IllegalArgumentException("Проект с ID " + task.getProject() + " не найден")))
                .createdByUser(userRepository.findById(task.getCreatedByUser())
                        .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + task.getCreatedByUser() + " не найден")))
                .createdAt(new Date())
                .updatedAt(new Date())
                .status(false)
                .acceptedForExecutionByUser(null)
                .build();
        TaskEntity savedTask = taskRepository.saveAndFlush(taskEntity);
        return getTask(savedTask.getTaskId());
    }

    public TaskForm updateTask(Long id, TaskUpdateDto task) {
        Optional<TaskEntity> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            TaskEntity taskEntity = taskOptional.get();
            taskEntity.setTitleTask(task.getTitleTask());
            taskEntity.setDescriptionTask(task.getDescriptionTask());
            taskEntity.setProject(projectRepository.findById(task.getProject())
                    .orElseThrow(() -> new IllegalArgumentException("Проект с ID " + task.getProject() + " не найден")));
            taskEntity.setStatus(task.getStatus());
            taskEntity.setAcceptedForExecutionByUser(task.getAcceptedForExecutionByUser() != 0
                    ? userRepository.findById(task.getAcceptedForExecutionByUser())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + task.getAcceptedForExecutionByUser() + " не найден"))
                    : null);
            taskEntity.setUpdatedAt(new Date());
            taskRepository.saveAndFlush(taskEntity);
            return getTask(id);
        }
        throw new IllegalArgumentException("Задача с ID " + id + " не найдена");
    }

    public TaskForm completeTask(Long id) {
        Optional<TaskEntity> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            TaskEntity taskEntity = taskOptional.get();
            if (taskEntity.getStatus()) {
                throw new IllegalStateException("Задача с ID " + id + " уже завершена");
            }
            taskEntity.setStatus(true);
            taskEntity.setUpdatedAt(new Date());
            taskRepository.saveAndFlush(taskEntity);
            return getTask(id);
        }
        throw new IllegalArgumentException("Задача с ID " + id + " не найдена");
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException("Задача с ID " + id + " не найдена");
        }
        taskRepository.deleteById(id);
    }

    public TaskForm assign(Long id, TasklAssignDto task) {
        Optional<TaskEntity> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            TaskEntity taskEntity = taskOptional.get();
            taskEntity.setAcceptedForExecutionByUser(userRepository.findById(task.getAcceptedForExecutionByUser())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + task.getAcceptedForExecutionByUser() + " не найден")));
            taskRepository.saveAndFlush(taskEntity);
            return getTask(id);
        }
        throw new IllegalArgumentException("Задача с ID " + id + " не найдена");
    }

    public List<TaskForm> getTasksByUser(Long userId) {
        return taskRepository.findAll()
                .stream()
                .filter(t -> t.getAcceptedForExecutionByUser() != null && t.getAcceptedForExecutionByUser().getUserId().equals(userId))
                .map(taskEntity -> {
                    List<TaskCommentForm> comments = commentToTaskRepository.findByTask(taskEntity)
                            .stream()
                            .map(comment -> TaskCommentForm.builder()
                                    .commentId((long) comment.getComment_id())
                                    .description(comment.getDescription())
                                    .createdByUser(taskEntity.getCreatedByUser().getUserId())
                                    .createdAt(comment.getCreated_at())
                                    .build())
                            .collect(Collectors.toList());
                    TaskForm taskForm = FactoryTask.GetTaskForm(taskEntity);
                    taskForm.setComments(comments);
                    return taskForm;
                })
                .collect(Collectors.toList());
    }

    public TaskCommentForm addComment(Long taskId, TaskCommentDto commentDto) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача с ID " + taskId + " не найдена"));

        CommentToTask comment = new CommentToTask();
        comment.setDescription(commentDto.getDescription());
        comment.setTask(taskEntity);
        comment.setCreated_at(new Date());

        CommentToTask savedComment = commentToTaskRepository.save(comment);

        return TaskCommentForm.builder()
                .commentId((long) savedComment.getComment_id())
                .description(savedComment.getDescription())
                .createdByUser(commentDto.getCreatedByUser())
                .createdAt(savedComment.getCreated_at())
                .build();
    }
}