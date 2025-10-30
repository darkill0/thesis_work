package org.example.restapi.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.components.kafka.EventsNotification;
import org.example.workspacelib.dto.create.ProjectCreateDto;
import org.example.workspacelib.dto.form.ProjectForm;
import org.example.workspacelib.dto.form.UserForm;
import org.example.workspacelib.dto.update.ProjectUpdateDto;
import org.example.workspacelib.kafkacon.KafkaWorkWithBroker;
import org.example.workspacelib.models.Project;
import org.example.workspacelib.service.FollowerToProjectService;
import org.example.workspacelib.service.ProjectService;
import org.example.workspacelib.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "API контроллер для проектов")
@RestController
@RequestMapping("api/project")
@Controller
public class ProjectController {



    private final String project_create =  "create";
    private final String project_delete = "delete/{id}";
    private final String project_update = "update/{id}";
    private final String project_get = "get/{id}";
    private final String project_list = "list";
    private final String project_list_by = "list/by/{id}";
    private final String follow_to_project = "follow/{projectId}/{userId}";
    private final String unfollow_to_project = "unfollow/{projectId}/{userId}";




    private final ProjectService projectService;
    private final KafkaWorkWithBroker kafkaWorkWithBroker;
    private final UserService userService;
    private final FollowerToProjectService followerToProjectService;

    @Autowired
    public ProjectController(ProjectService projectService, KafkaWorkWithBroker kafkaWorkWithBroker, UserService userService, FollowerToProjectService followerToProjectService) {
        this.projectService = projectService;
        this.kafkaWorkWithBroker = kafkaWorkWithBroker;
        this.userService = userService;
        this.followerToProjectService = followerToProjectService;
    }

    @Operation(summary = "Создание проекта", description = "Эндпоинт для создания нового проекта")
    @PostMapping(project_create)
    public ResponseEntity<Project> createProject(  @RequestBody ProjectCreateDto project) {
        try {
            Project project1 = projectService.createProject(project);
            if (project1 != null) {
                for(UserForm u : userService.getUsersWithRoleAdmin()){
                    kafkaWorkWithBroker.sendMessage("Создан проект + ["+project1.toString()+"]", EventsNotification.CREATE_PROJECT, Math.toIntExact(project.getCreatedByUser()), Math.toIntExact(u.getUserId()));
                }
                return  ResponseEntity.ok(project1);

            }
            return ResponseEntity.badRequest().build();


        }catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }


    }

    @Operation(summary = "Удаление проекта", description = "Эндпоинт для удаления проекта")
    @DeleteMapping(project_delete)
    public ResponseEntity<Project> deleteProject(@PathVariable Long id) {
        try{
            ProjectForm f = projectService.getProject(id);
            projectService.deleteProject(id);
            for(UserForm u : userService.getUsersWithRoleAdmin()){
                kafkaWorkWithBroker.sendMessage("Удален проект c id "+id.toString(), EventsNotification.CLOSE_PROJECT, Math.toIntExact(f.getCreatedByUser()), Math.toIntExact(u.getUserId()));

            }
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Обновление проекта", description = "Эндпоинт для обновление проекта")
    @PostMapping(project_update)
    public ResponseEntity<ProjectForm> updateProject( @PathVariable Long id,  @RequestBody ProjectUpdateDto project) {
        try {
            ProjectForm projectForm = projectService.updateProject(id, project);
            if(projectForm != null) {

                List<UserForm> ns = followerToProjectService.getFollowerToProjects(projectForm.getProjectId());
                for(UserForm u : ns){
                    kafkaWorkWithBroker.sendMessage("Обновлен проект + ["+projectForm.toString()+"]", EventsNotification.UPDATE_PROJECT, Math.toIntExact(projectForm.getCreatedByUser()), Math.toIntExact(u.getUserId()));

                }
                return  ResponseEntity.ok(projectForm);
            }
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение проекта по id", description = "Эндпоинт для получения проекта")
    @GetMapping(project_get)
    public ResponseEntity<ProjectForm> getProject( @PathVariable Long id) {
        ProjectForm projectForm = projectService.getProject(id);
        if(projectForm != null) {
            return ResponseEntity.ok(projectForm);

        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("follow/{id}")
    public ResponseEntity<List<ProjectForm>> getProjectsFollowersToUser(@PathVariable Long id) {
        return ResponseEntity.ok(followerToProjectService.getProjectsToUser(id));
    }

    @Operation(summary = "Получение проектов", description = "Эндпоинт для получения проектов")
    @GetMapping(project_list)
    public ResponseEntity<List<ProjectForm>> getAllProject() {

        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @Operation(summary = "Получение проектов пользователя", description = "Эндпоинт для получения проектов")
    @GetMapping(project_list_by)
    public ResponseEntity<List<ProjectForm>> getAllByProject(@PathVariable Long id) {

        return ResponseEntity.ok(projectService.getAllProjectsBy(id));
    }

    @Operation(summary = "Подписка на проект", description = "Эндпоинт для подписки на проект")
    @GetMapping(follow_to_project)
    public ResponseEntity<Boolean> followToProject(@PathVariable Long projectId, @PathVariable Long userId) {

        return ResponseEntity.ok(followerToProjectService.followProject(userId, projectId));
    }

    @Operation(summary = "Отписка от проекта", description = "Эндпоинт для отписки от проекта")
    @GetMapping(unfollow_to_project)
    public ResponseEntity<Boolean> unfollowToProject(@PathVariable Long projectId, @PathVariable Long userId) {

        return ResponseEntity.ok(followerToProjectService.unfollowProject(userId, projectId));
    }

}
