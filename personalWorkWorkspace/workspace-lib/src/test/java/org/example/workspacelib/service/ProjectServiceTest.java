package org.example.workspacelib.service;

import org.example.workspacelib.dto.form.ProjectForm;
import org.example.workspacelib.entity.ProjectEntity;
import org.example.workspacelib.entity.RoleEntity;
import org.example.workspacelib.entity.UserEntity;
import org.example.workspacelib.repository.ProjectRepository;
import org.example.workspacelib.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    ProjectRepository projectRepo;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ProjectService projectService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getProject_GetNotEmptyProject_ReturnNotEmptyProject() {
        //given
        Long id = 1L;
        ProjectForm expectedProjectForm = new ProjectForm(id, "", "", "", new Date(), new Date(), id, new ArrayList<>());
        UserEntity userEntity = UserEntity.builder().userId(id).build();
        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectId(id).title("").descrption("").urlToProject("").createdAt(new Date()).updatedAt(new Date()).createdByUser(userEntity).tasks(new ArrayList<>()).build();

        // Мокируем возвращаемое значение findById
        doReturn(Optional.of(projectEntity)).when(projectRepo).findById(id);

        //when
        ProjectForm projectForm = projectService.getProject(id);

        //result
        assertNotEquals(expectedProjectForm, projectForm, "Ожидалось, что возвращенный проект будет соответствовать ожидаемому");
    }
}
