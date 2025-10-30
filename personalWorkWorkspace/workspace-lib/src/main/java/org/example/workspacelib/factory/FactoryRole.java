package org.example.workspacelib.factory;

import org.example.workspacelib.dto.form.RoleForm;
import org.example.workspacelib.entity.RoleEntity;
import org.example.workspacelib.models.Role;

public class  FactoryRole {

    public static Role  GetRoleModel(RoleEntity roleEntity){
        return Role.builder()
                .roleId(roleEntity.getRoleId())
                .role(roleEntity.getRole())
                .users(roleEntity.getUsers().stream().map(FactoryUser::GetUser).toList())
                .build();
    }

    public static RoleEntity  GetRoleEntity(Role role){
        return RoleEntity.builder()
                .roleId(role.getRoleId())
                .role(role.getRole())
                .users(role.getUsers().stream().map(FactoryUser::GetUserEntity).toList())
                .build();
    }

    public static RoleForm GetRoleForm(RoleEntity role){
        return RoleForm.builder()
                .roleId(role.getRoleId())
                .role(role.getRole())
                .users(role.getUsers().stream().map(FactoryUser::GetUserForm).toList())
                .build();
    }

}
