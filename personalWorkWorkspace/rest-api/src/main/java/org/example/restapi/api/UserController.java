package org.example.restapi.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.workspacelib.dto.create.UserCreateDto;
import org.example.workspacelib.dto.form.UserForm;
import org.example.workspacelib.dto.update.UserUpdateForm;
import org.example.workspacelib.service.UserService;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.ws.rs.core.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Контроллер для управления пользователями")
@RestController
@RequestMapping("/api/users")
public class  UserController {
    private final UserService userService;
    private final Keycloak keycloak;
    private final String uploadPath = "C:\\Users\\Илья\\IdeaProjects\\personalWorkWorkspace\\rest-api\\src\\main\\resources\\static";



    public UserController(UserService userService) {
        this.userService = userService;
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:18080")
                .realm("sandbox")
                .clientId("front-end")
                .grantType(OAuth2Constants.PASSWORD)
                .username("test")
                .password("test")
                .build();

    }

    private final String user_create = "create";
    private final String user_update = "update/{id}";
    private final String user_delete = "delete/{id}";
    private final String user_get = "get/{id}";
    private final String user_list = "list";


    @Operation(summary = "Создание пользователя", description = "Эндпоинт для создания нового пользователя")
    @PostMapping(user_create)
    public ResponseEntity<UserForm> createUser(@RequestBody UserCreateDto userDto) {
        List<CredentialRepresentation> credList = new ArrayList<>();
        List<String> clientRoles = new ArrayList<>();

        UserForm s = userService.createUser(userDto);
        Long roleId = userDto.getRoleId();

        // Назначение клиентских ролей на основе roleId
        if (roleId == 1) {
            clientRoles.add("ROLE_ADMIN");
        } else if (roleId == 2) {
            clientRoles.add("ROLE_MANAGER");
        } else if (roleId == 3) {
            clientRoles.add("ROLE_USER");
        }

        // Создание объекта пользователя
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userDto.getLogin());
        userRepresentation.setEnabled(true);

        // Установка пароля
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userDto.getPasswordUser());
        credentialRepresentation.setTemporary(false); // Установи временный пароль или нет

        credList.add(credentialRepresentation);
        userRepresentation.setCredentials(credList);

        try {
            // Создание пользователя
            Response response = keycloak.realm("sandbox").users().create(userRepresentation);
            System.out.println("Keycloak response status: " + response.getStatus());

            if (response.getStatus() == 201) {
                // Получение ID созданного пользователя
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                System.out.println("Создан пользователь с ID: " + userId);

                // Получение ресурсов realm и клиента
                RealmResource realmResource = keycloak.realm("sandbox");

                // Поиск клиента по его имени и получение clientId
                ClientRepresentation clientRepresentation = realmResource.clients().findByClientId("front-end").get(0);
                String clientId = clientRepresentation.getId();
                ClientResource clientResource = realmResource.clients().get(clientId); // Получаем ClientResource

                UsersResource usersResource = realmResource.users();

                // Назначение клиентских ролей пользователю
                for (String roleName : clientRoles) {
                    RoleRepresentation clientRole = clientResource.roles().get(roleName).toRepresentation();
                    System.out.println("Назначение клиентской роли: " + clientRole.getName());

                    // Присвоение client roles
                    usersResource.get(userId).roles().clientLevel(clientId).add(Collections.singletonList(clientRole));
                    System.out.println("Назначена роль: " + roleName);
                }
            } else {
                System.out.println("Ошибка при создании пользователя: " + response.getStatusInfo());
            }
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(s);
    }



    @Operation(summary = "Обновление роли пользователя", description = "Эндпоинт для обновления роли пользователя")
    @PostMapping(user_update)
    public ResponseEntity<UserForm> updateUser(@PathVariable Long id, @RequestBody UserUpdateForm userDto) {
        List<String> clientRoles = new ArrayList<>();
        Long roleId = userDto.getRoleId();

        // Определение новых клиентских ролей на основе roleId
        if (roleId == 1) {
            clientRoles.add("ROLE_ADMIN");
        } else if (roleId == 2) {
            clientRoles.add("ROLE_MANAGER");
        } else if (roleId == 3) {
            clientRoles.add("ROLE_USER");
        }

        UserResource user = null;
        String login = userService.getUser(id).getLogin();

        // Получение пользователя по логину
        List<UserRepresentation> users = keycloak.realm("sandbox").users().list();
        for (UserRepresentation u : users) {
            if (u.getUsername().equals(login)) {
                user = keycloak.realm("sandbox").users().get(u.getId());
                break;
            }
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Пользователь не найден
        }

        RealmResource realmResource = keycloak.realm("sandbox");
        ClientRepresentation clientRepresentation = realmResource.clients().findByClientId("front-end").get(0);
        String clientId = clientRepresentation.getId();
        ClientResource clientResource = realmResource.clients().get(clientId); // Получаем ClientResource

        // Удаление предыдущих клиентских ролей
        List<RoleRepresentation> existingRoles = user.roles().clientLevel(clientId).listAll();
        for (RoleRepresentation existingRole : existingRoles) {
            if (!clientRoles.contains(existingRole.getName())) {
                user.roles().clientLevel(clientId).remove(Collections.singletonList(existingRole));
                System.out.println("Удалена роль: " + existingRole.getName());
            }
        }

        // Назначение новых клиентских ролей
        for (String roleName : clientRoles) {
            RoleRepresentation clientRole = clientResource.roles().get(roleName).toRepresentation();
            System.out.println("Назначение клиентской роли: " + clientRole.getName());

            // Присвоение клиентских ролей
            user.roles().clientLevel(clientId).add(Collections.singletonList(clientRole));
            System.out.println("Назначена роль: " + roleName);
        }

        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }


    @Operation(summary = "Удаление пользователя", description = "Эндпоинт для удаления пользователя")
    @DeleteMapping(user_delete)
    public Map<String, Boolean> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Collections.singletonMap("user", Boolean.TRUE);
        } catch (Exception e) {
            System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
            return Collections.singletonMap("user", Boolean.FALSE);
        }
    }

    @GetMapping("keycloak")
    public List<UserRepresentation> getUsers(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");

        try {
            Keycloak keycloakClient = KeycloakBuilder.builder()
                    .serverUrl("http://localhost:18080/")
                    .realm("sandbox")
                    .authorization(token)
                    .clientId("front-end")
                    .build();

            return keycloakClient.realm("sandbox").users().list();
        } catch (Exception e) {
            System.err.println("Ошибка при получении пользователей: " + e.getMessage());
            return List.of();
        }
    }
    @GetMapping("/keycloak/online-users")
    public List<UserRepresentation> getOnlineUsers(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");

        try {
            Keycloak keycloakClient = KeycloakBuilder.builder()
                    .serverUrl("http://localhost:18080/")
                    .realm("sandbox")
                    .authorization(token)
                    .clientId("front-end")
                    .build();

            // Получение всех пользователей
            List<UserRepresentation> allUsers = keycloakClient.realm("sandbox").users().list();

            // Получение всех активных сессий
            List<UserSessionRepresentation> activeSessions = allUsers.stream()
                    .flatMap(user -> keycloakClient.realm("sandbox").users()
                            .get(user.getId()).getUserSessions().stream()
                    )
                    .collect(Collectors.toList());

            // Фильтрация пользователей, которые имеют активные сессии
            List<String> onlineUserIds = activeSessions.stream()
                    .map(UserSessionRepresentation::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            return allUsers.stream()
                    .filter(user -> onlineUserIds.contains(user.getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Ошибка при получении онлайн-пользователей: " + e.getMessage());
            return List.of();
        }
    }

    @Operation(summary = "Получение одного пользователя", description = "Эндпоинт для получения пользователя")
    @GetMapping(user_get)
    public ResponseEntity<UserForm> getUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUser(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение всех пользователей", description = "Эндпоинт для всех пользователей")
    @GetMapping(user_list)
    public ResponseEntity<List<UserForm>> getListUser() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/findByLogin/{login}")
    public ResponseEntity<UserForm> findUserByLogin(@PathVariable String login) {
        try {
            UserForm user = userService.findUserByLogin(login);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение активных сессий пользователей", description = "Эндпоинт для получения активных сессий пользователей для клиента front-end")
    @GetMapping("/active-sessions")
    public ResponseEntity<List<Map<String, Object>>> getActiveSessions() {
        try {
            // Получение ресурсов realm и клиента
            RealmResource realmResource = keycloak.realm("sandbox");

            // Поиск клиента по его имени и получение clientId
            ClientRepresentation clientRepresentation = realmResource.clients().findByClientId("front-end").get(0);
            String clientId = clientRepresentation.getId();
            ClientResource clientResource = realmResource.clients().get(clientId);

            // Получение активных сессий для клиента
            List<UserSessionRepresentation> sessions = clientResource.getUserSessions(0, 100);

            // Преобразование сессий в более читабельный формат
            List<Map<String, Object>> sessionData = new ArrayList<>();
            for (UserSessionRepresentation session : sessions) {
                Map<String, Object> sessionInfo = Map.of(

                        "userId", session.getUserId(),
                        "userName", session.getUsername(),
                        "ipAddress", session.getIpAddress(),
                        "started", session.getStart(),
                        "lastAccess", session.getLastAccess(),
                        "clients", session.getClients()
                );
                sessionData.add(sessionInfo);
            }

            return ResponseEntity.ok(sessionData);
        } catch (Exception e) {
            System.err.println("Ошибка при получении активных сессий: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Обновление имени пользователя", description = "Эндпоинт для обновления имени пользователя")
    @PostMapping("/{id}/updateUsername")
    public ResponseEntity<String> updateUsername(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newUsername = request.get("username");
        String login = userService.getUser(id).getLogin();

        try {
            // Получение пользователя в базе данных
            UserForm user = userService.findUserByLogin(login);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            // Обновление имени пользователя в базе данных
            userService.updateUserLogin(id, newUsername);

            // Обновление имени пользователя в Keycloak
            List<UserRepresentation> users = keycloak.realm("sandbox").users().list();
            UserResource userResource = null;

            for (UserRepresentation u : users) {
                if (u.getUsername().equals(login)) {
                    userResource = keycloak.realm("sandbox").users().get(u.getId());
                    break;
                }
            }

            if (userResource != null) {
                UserRepresentation userRepresentation = userResource.toRepresentation();
                userRepresentation.setUsername(newUsername);
                userResource.update(userRepresentation);
                return ResponseEntity.ok("Имя пользователя успешно обновлено");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь в Keycloak не найден");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении пользователя");
        }
    }

    @PostMapping("/{id}/uploadAvatar")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id, @RequestParam("avatar") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл не выбран");
        }

        try {
            // Определяем директорию для сохранения
            String originalFilename = file.getOriginalFilename();
            String filePath = uploadPath + "/" + originalFilename;
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent()); // Создание директории, если она отсутствует
            file.transferTo(new File(filePath)); // Сохраняем файл

            // Сохраняем путь к файлу в базе данных
            String avatarUrl = "/uploads/" + originalFilename; // URL для доступа к аватару
            // Обновляем профиль пользователя
            userService.updateAvatarUrl(id, avatarUrl);

            // Возвращаем ссылку на загруженный файл
            return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка при сохранении файла");
        }
    }

    @Operation(summary = "Обновление email пользователя", description = "Эндпоинт для обновления email пользователя")
    @PostMapping("/{id}/updateEmail")
    public ResponseEntity<String> updateEmail(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newEmail = request.get("email");
        String login = userService.getUser(id).getLogin();

        try {
            // Получение пользователя в базе данных
            UserForm user = userService.findUserByLogin(login);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }


            // Обновление email в Keycloak
            List<UserRepresentation> users = keycloak.realm("sandbox").users().list();
            UserResource userResource = null;

            for (UserRepresentation u : users) {
                if (u.getUsername().equals(login)) {
                    userResource = keycloak.realm("sandbox").users().get(u.getId());
                    break;
                }
            }

            if (userResource != null) {
                UserRepresentation userRepresentation = userResource.toRepresentation();
                userRepresentation.setEmail(newEmail);
                userResource.update(userRepresentation);
                return ResponseEntity.ok("Email успешно обновлен");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь в Keycloak не найден");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении email");
        }
    }

    @Operation(summary = "Обновление пароля пользователя", description = "Эндпоинт для обновления пароля пользователя в базе данных и в Keycloak")
    @PostMapping("/{id}/updatePassword")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> passwordRequest) {
        String currentPassword = passwordRequest.get("currentPassword");
        String newPassword = passwordRequest.get("newPassword");
        String confirmPassword = passwordRequest.get("confirmPassword");

        // Проверка на совпадение нового пароля с подтверждением
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("Новый пароль и подтверждение пароля не совпадают.");
        }

        String login = userService.getUser(id).getLogin();

        try {
            // Получение пользователя из Keycloak
            List<UserRepresentation> users = keycloak.realm("sandbox").users().list();
            UserResource userResource = null;

            for (UserRepresentation user : users) {
                if (user.getUsername().equals(login)) {
                    userResource = keycloak.realm("sandbox").users().get(user.getId());
                    break;
                }
            }

            if (userResource == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь в Keycloak не найден.");
            }

            // Обновление пароля в Keycloak
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(newPassword);
            credentialRepresentation.setTemporary(false); // Устанавливаем временный пароль или нет

            // Проверка текущего пароля перед обновлением
            List<CredentialRepresentation> credentials = userResource.credentials();
            boolean isCurrentPasswordCorrect = credentials.stream()
                    .anyMatch(cred -> currentPassword.equals(cred.getValue())); // Здесь может потребоваться дополнительная проверка, в зависимости от настройки Keycloak.

            if (!isCurrentPasswordCorrect) {
                return ResponseEntity.badRequest().body("Неверный текущий пароль.");
            }

            // Обновление пароля в Keycloak
            userResource.resetPassword(credentialRepresentation);
            System.out.println("Пароль пользователя в Keycloak успешно обновлён.");

            // Обновление пароля в базе данных
            userService.updateUserPassword(id, newPassword);
            return ResponseEntity.ok("Пароль успешно обновлён.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении пароля.");
        }
    }


}
