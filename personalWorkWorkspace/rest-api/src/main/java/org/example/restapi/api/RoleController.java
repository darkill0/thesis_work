package org.example.restapi.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.workspacelib.dto.create.RoleCreateDto;
import org.example.workspacelib.dto.form.RoleForm;
import org.example.workspacelib.dto.update.RoleUpdateDto;
import org.example.workspacelib.models.Role;
import org.example.workspacelib.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Контроллер для ролей")
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final String role_create =  "create";
    private final String role_delete = "delete/{id}";
    private final String role_update = "update/{id}";
    private final String role_get = "get/{id}";
    private final String role_list = "list";




    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Создание роли", description = "Эндпоинт для создания новой роли")
    @PostMapping(role_create)
    public ResponseEntity<RoleForm> createrole(@RequestBody RoleCreateDto role) {
        return ResponseEntity.ok(roleService.createRole(role));
    }

    @Operation(summary = "Удаление роли", description = "Эндпоинт для удаления роли")
    @DeleteMapping(role_delete)
    public ResponseEntity<RoleForm> deleterole(@PathVariable Long id) {
        try{
            roleService.deleteRole(id);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Обновление роли", description = "Эндпоинт для обновление роли")
    @PostMapping(role_update)
    public ResponseEntity<RoleForm> updaterole( @PathVariable Long id,  @RequestBody RoleUpdateDto role) {
        return ResponseEntity.ok(roleService.updateRole(id, role));
    }

    @Operation(summary = "Получение роли по id", description = "Эндпоинт для получения роли")
    @GetMapping(role_get)
    public ResponseEntity<RoleForm> getrole( @PathVariable Long id) {

        return ResponseEntity.ok(roleService.getRole(id));
    }

    @Operation(summary = "Получение ролей", description = "Эндпоинт для получения ролей")
    @GetMapping(role_list)
    public ResponseEntity<List<RoleForm>> getAllrole() {

        return ResponseEntity.ok(roleService.getAllRoles());
    }

}
