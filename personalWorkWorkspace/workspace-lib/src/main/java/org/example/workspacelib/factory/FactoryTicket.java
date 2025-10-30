package org.example.workspacelib.factory;

import org.example.workspacelib.dto.form.CommentForm;
import org.example.workspacelib.dto.form.TicketForm;
import org.example.workspacelib.entity.CommentEntity;
import org.example.workspacelib.entity.TicketEntity;
import org.example.workspacelib.models.Comment;
import org.example.workspacelib.models.Ticket;

import java.util.stream.Collectors;

public class FactoryTicket {

    public static TicketForm GetTicketForm(TicketEntity entity) {
        return TicketForm.builder()
                .ticketId(entity.getTicketId())
                .descrption(entity.getDescrption())
                .urlToProject(entity.getUrlToProject())
                .createdByUser(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getUserId() : null)
                .acceptedByUser(entity.getAcceptedByUser() != null ? entity.getAcceptedByUser().getUserId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .status(entity.getStatus())
                .comments(entity.getComments() != null ? entity.getComments().stream()
                        .map(FactoryTicket::GetCommentForm)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public static CommentForm GetCommentForm(CommentEntity entity) {
        return CommentForm.builder()
                .commentId(entity.getCommentId())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .createdByUser(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getUserId() : null)
                .build();
    }

    public static Ticket GetTicket(TicketEntity entity) {
        return Ticket.builder()
                .ticketId(entity.getTicketId())
                .descrption(entity.getDescrption())
                .urlToProject(entity.getUrlToProject())
                .createdByUser(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getUserId() : null)
                .acceptedByUser(entity.getAcceptedByUser() != null ? entity.getAcceptedByUser().getUserId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .status(entity.getStatus())
                .comments(entity.getComments() != null ? entity.getComments().stream()
                        .map(FactoryTicket::GetComment)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public static Comment GetComment(CommentEntity entity) {
        return Comment.builder()
                .commentId(entity.getCommentId())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .createdByUser(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getUserId() : null)
                .build();
    }

    public static TicketEntity GetTicketEntity(Ticket ticket) {
        TicketEntity entity = TicketEntity.builder()
                .ticketId(ticket.getTicketId())
                .descrption(ticket.getDescrption())
                .urlToProject(ticket.getUrlToProject())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .status(ticket.getStatus())
                .comments(ticket.getComments() != null ? ticket.getComments().stream()
                        .map(FactoryTicket::GetCommentEntity)
                        .collect(Collectors.toList()) : null)
                .build();

        // Устанавливаем createdByUser и acceptedByUser, если они не null
        // Примечание: Это предполагает, что UserEntity нужно загрузить из базы
        // В реальном приложении здесь может потребоваться доступ к UserRepository
        // Для простоты оставим null, так как userId передаётся через DTO
        entity.setCreatedByUser(null);
        entity.setAcceptedByUser(null);

        return entity;
    }

    public static CommentEntity GetCommentEntity(Comment comment) {
        CommentEntity entity = CommentEntity.builder()
                .commentId(comment.getCommentId())
                .description(comment.getDescription())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .build();

        // Аналогично, createdByUser устанавливается как null
        // В реальном приложении может потребоваться загрузка UserEntity
        entity.setCreatedByUser(null);

        return entity;
    }
}