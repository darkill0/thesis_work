package org.example.workspacelib.service;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.workspacelib.dto.create.UserCreateDto;
import org.example.workspacelib.dto.form.UserForm;
import org.example.workspacelib.dto.update.UserUpdateForm;
import org.example.workspacelib.entity.UserEntity;
import org.example.workspacelib.factory.FactoryUser;
import org.example.workspacelib.models.User;
import org.example.workspacelib.repository.RoleRepository;
import org.example.workspacelib.repository.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;

    private final Keycloak keycloakClient;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, Keycloak keycloakClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.keycloakClient = keycloakClient;

    }

    public void saveUser(String login){
        UserEntity userEntity = new UserEntity();
        userEntity.setLogin(login);
        userRepository.save(userEntity);
    }

    public UserForm getUser(Long id) {
        return userRepository.findById(id).map(FactoryUser::GetUserForm).orElse(null);
    }

    public List<UserForm> getAllUsers() {
        return userRepository.findAll().stream().map(FactoryUser::GetUserForm).toList();

    }
    @Transactional
    public UserForm createUser(UserCreateDto userDto) {
        UserEntity user = UserEntity.builder()
                .login(userDto.getLogin())
                .passwordUser(userDto.getPasswordUser())
                .roleId(roleRepository.findById(userDto.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found")))
                .createdAt(new Date())
                .updatedAt(new Date())
                .tasksCreatedByUser(null)
                .acceptedTasksByUser(null)
                .ticketsCreatedByUser(null)
                .projects(null)
                .build();
        // Сохранение пользователя в БД
        userRepository.saveAndFlush(user);
        return FactoryUser.GetUserForm(user);
    }



    public UserForm updateUser(Long id, UserUpdateForm user) {
        if(userRepository.findById(id).isPresent()) {
            UserEntity userEntity = userRepository.findById(id).get();
            userEntity.setRoleId(roleRepository.findById(user.getRoleId()).get());
            return FactoryUser.GetUserForm(userRepository.saveAndFlush(userEntity));
        }
        return null;
    }

    public List<UserForm> getUsersWithRoleAdmin() {
        return userRepository.findAll().stream().filter(u -> u.getRoleId().getRole().equalsIgnoreCase("admin")).map(FactoryUser::GetUserForm).toList();
    }

    public UserForm updateUserLogin(Long id, String newUserName) {
        if(userRepository.findById(id).isPresent()) {
            UserEntity userEntity = userRepository.findById(id).get();
            userEntity.setLogin(newUserName);
            return FactoryUser.GetUserForm(userRepository.saveAndFlush(userEntity));
        }
        return null;
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserForm findUserByLogin(String login) {
        UserEntity user = userRepository.findByLogin(login);
        if(user != null){
            return FactoryUser.GetUserForm(user);
        }
        else return null;
    }

    public void updateAvatarUrl(Long id, String avatarUrl) {
        UserEntity user = userRepository.findById(id).get();
        user.setUrl(avatarUrl);
        userRepository.saveAndFlush(user);
    }


    public void updateUserPassword(Long id, String newPassword) {
        UserEntity user = userRepository.findById(id).get();
        user.setPasswordUser(newPassword);
        userRepository.saveAndFlush(user);
    }
}
