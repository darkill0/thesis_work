package org.example.workspacelib.service;

import org.example.workspacelib.dto.create.RoleCreateDto;
import org.example.workspacelib.dto.form.RoleForm;
import org.example.workspacelib.dto.update.RoleUpdateDto;
import org.example.workspacelib.entity.RoleEntity;
import org.example.workspacelib.factory.FactoryRole;
import org.example.workspacelib.models.Role;
import org.example.workspacelib.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository repo;

    @Autowired
    public RoleService(RoleRepository repo) {
        this.repo = repo;
    }

    public RoleForm createRole(RoleCreateDto createDto) {
        RoleEntity role = new RoleEntity();
        role.setRole(createDto.getRole());
        return (FactoryRole.GetRoleForm(repo.saveAndFlush(role)));

    }

    public RoleForm updateRole(Long id, RoleUpdateDto name) {
        if(repo.existsById(id)) {
            RoleEntity role = repo.findById(id).get();
            role.setRole(name.getRole());
            return (FactoryRole.GetRoleForm(repo.saveAndFlush(role)));
        }
        return (FactoryRole.GetRoleForm(repo.findById(id).get()));


    }

    public List<RoleForm> getAllRoles(){
        List<RoleEntity> roles = repo.findAll();
        return roles.stream().map(FactoryRole::GetRoleForm).toList();
    }

    public void deleteRole(Long id) {
        try{
            repo.deleteById(id);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public RoleForm getRole(Long id) {
        return repo.findById(id).map(FactoryRole::GetRoleForm).orElse(null);
    }

}
