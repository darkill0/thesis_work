package org.example.workspacelib.repository;

import org.example.workspacelib.entity.TicketEntity;
import org.example.workspacelib.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByCreatedByUser(UserEntity createdByUser);
    List<TicketEntity> findByStatus(int s);
    List<TicketEntity> findByacceptedByUser(UserEntity acceptedUserId);
}
