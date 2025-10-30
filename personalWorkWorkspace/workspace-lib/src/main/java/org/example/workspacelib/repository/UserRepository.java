package org.example.workspacelib.repository;

import org.example.workspacelib.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByLogin(String login);

}
