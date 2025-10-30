package org.example.workspacelib.service;

import org.example.workspacelib.dto.create.CommentCreateDto;
import org.example.workspacelib.dto.create.TicketCreateDto;
import org.example.workspacelib.dto.form.TicketForm;
import org.example.workspacelib.dto.update.TicketUpdateDto;
import org.example.workspacelib.entity.CommentEntity;
import org.example.workspacelib.entity.TicketEntity;
import org.example.workspacelib.factory.FactoryTicket;
import org.example.workspacelib.repository.CommentRepository;
import org.example.workspacelib.repository.TicketRepository;
import org.example.workspacelib.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public TicketForm getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(FactoryTicket::GetTicketForm)
                .orElse(null);
    }

    public List<TicketForm> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(FactoryTicket::GetTicketForm)
                .collect(Collectors.toList());
    }

    public TicketForm createTicket(TicketCreateDto ticket) {
        TicketEntity ticketEntity = TicketEntity.builder()
                .descrption(ticket.getDescrption())
                .urlToProject(ticket.getUrlToProject())
                .createdByUser(userRepository.findById(ticket.getCreatedByUser()).orElseThrow(() -> new RuntimeException("User not found")))
                .updatedAt(new Date())
                .createdAt(new Date())
                .status(3)
                .acceptedByUser(null)
                .comments(new ArrayList<>())
                .build();
        return FactoryTicket.GetTicketForm(ticketRepository.saveAndFlush(ticketEntity));
    }

    public TicketForm updateTicket(Long id, TicketUpdateDto ticket) {
        if (ticketRepository.existsById(id)) {
            TicketEntity ticketEntity = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
            ticketEntity.setDescrption(ticket.getDescrption());
            ticketEntity.setUrlToProject(ticket.getUrlToProject());
            ticketEntity.setUpdatedAt(new Date());
            return FactoryTicket.GetTicketForm(ticketRepository.saveAndFlush(ticketEntity));
        }
        return null;
    }

    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }

    public List<TicketForm> getCompletedTickets() {
        List<TicketEntity> completedTickets = ticketRepository.findByStatus(3);
        return completedTickets.stream()
                .map(FactoryTicket::GetTicketForm)
                .collect(Collectors.toList());
    }

    public List<TicketForm> getMyTickets(Long id) {
        List<TicketEntity> myTickets = ticketRepository.findByCreatedByUser(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
        return myTickets.stream()
                .map(FactoryTicket::GetTicketForm)
                .collect(Collectors.toList());
    }

    public List<TicketForm> getMyAcceptedTickets(Long id) {
        List<TicketEntity> myTickets = ticketRepository.findByacceptedByUser(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
        return myTickets.stream()
                .map(FactoryTicket::GetTicketForm)
                .collect(Collectors.toList());
    }

    public void takeTicket(Long id, Long userId) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setAcceptedByUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
        ticket.setStatus(2); // Обновить статус на "В работе"
        ticketRepository.save(ticket);
    }

    public CommentEntity addComment(Long ticketId, CommentCreateDto commentDto) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        CommentEntity comment = CommentEntity.builder()
                .description(commentDto.getDescription())
                .status(commentDto.getStatus())
                .createdAt(new Date())
                .ticket(ticket)
                .createdByUser(userRepository.findById(commentDto.getCreatedByUser())
                        .orElseThrow(() -> new RuntimeException("User not found")))
                .build();
        ticket.getComments().add(comment);
        ticketRepository.save(ticket);
        return comment;
    }
}