package org.example.workspacelib.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.workspacelib.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}
