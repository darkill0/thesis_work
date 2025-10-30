package org.example.restapi.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.components.kafka.EventsNotification;
import org.example.workspacelib.dto.create.CommentCreateDto;
import org.example.workspacelib.dto.create.TicketCreateDto;
import org.example.workspacelib.dto.form.TicketForm;
import org.example.workspacelib.dto.update.TicketUpdateDto;
import org.example.workspacelib.entity.CommentEntity;
import org.example.workspacelib.kafkacon.KafkaWorkWithBroker;
import org.example.workspacelib.service.TicketService;
import org.example.workspacelib.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Контроллер для тикетов")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final String ticket_create = "create";
    private final String ticket_delete = "delete/{id}";
    private final String ticket_update = "update/{id}";
    private final String ticket_get = "get/{id}";
    private final String ticket_list = "list";
    private final String ticket_my = "my/{id}";
    private final String ticket_my_accepted = "my/accepted/{id}";
    private final String ticket_completed = "completed";
    private final String ticket_comment_add = "comment/{ticketId}/add";

    private final TicketService ticketService;
    private final KafkaWorkWithBroker kafkaWorkWithBroker;
    private final UserService userService;

    @Autowired
    public TicketController(TicketService ticketService, KafkaWorkWithBroker kafkaWorkWithBroker, UserService userService) {
        this.ticketService = ticketService;
        this.kafkaWorkWithBroker = kafkaWorkWithBroker;
        this.userService = userService;
    }

    @Operation(summary = "Создание тикета", description = "Эндпоинт для создания нового тикета")
    @PostMapping(ticket_create)
    public ResponseEntity<TicketForm> createTicket(@RequestBody TicketCreateDto ticket) {
        try {
            TicketForm ticketForm = ticketService.createTicket(ticket);
            for (var f : userService.getAllUsers()) {
                kafkaWorkWithBroker.sendMessage("Создан тикет [" + ticketForm.toString() + "]",
                        EventsNotification.CREATE_TICKET,
                        Math.toIntExact(ticket.getCreatedByUser()),
                        Math.toIntExact(f.getUserId()));
            }
            return ResponseEntity.ok(ticketForm);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Удаление тикета", description = "Эндпоинт для удаления тикета")
    @DeleteMapping(ticket_delete)
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        try {
            TicketForm f = ticketService.getTicket(id);
            ticketService.deleteTicket(id);
            kafkaWorkWithBroker.sendMessage("Удален тикет с id " + id,
                    EventsNotification.CLOSE_TICKET,
                    Math.toIntExact(f.getAcceptedByUser() != null ? f.getAcceptedByUser() : f.getCreatedByUser()),
                    Math.toIntExact(f.getCreatedByUser()));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Обновление тикета", description = "Эндпоинт для обновления тикета")
    @PostMapping(ticket_update)
    public ResponseEntity<TicketForm> updateTicket(@PathVariable Long id, @RequestBody TicketUpdateDto dto) {
        try {
            TicketForm ticketForm = ticketService.updateTicket(id, dto);
            kafkaWorkWithBroker.sendMessage("Обновлен тикет [" + ticketForm.toString() + "]",
                    EventsNotification.UPDATE_TICKET,
                    Math.toIntExact(ticketForm.getCreatedByUser()),
                    Math.toIntExact(ticketForm.getAcceptedByUser() != null ? ticketForm.getAcceptedByUser() : ticketForm.getCreatedByUser()));
            return ResponseEntity.ok(ticketForm);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение тикета по id", description = "Эндпоинт для получения тикета")
    @GetMapping(ticket_get)
    public ResponseEntity<TicketForm> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @Operation(summary = "Получение тикетов", description = "Эндпоинт для получения тикетов")
    @GetMapping(ticket_list)
    public ResponseEntity<List<TicketForm>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @Operation(summary = "Получение тикетов текущего пользователя", description = "Эндпоинт для получения тикетов пользователя")
    @GetMapping(ticket_my)
    public ResponseEntity<List<TicketForm>> getMyTickets(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getMyTickets(id));
    }

    @Operation(summary = "Получение тикетов в выполнении текущего пользователя", description = "Эндпоинт для получения тикетов в работе пользователя")
    @GetMapping(ticket_my_accepted)
    public ResponseEntity<List<TicketForm>> getMyAcceptedTickets(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getMyAcceptedTickets(id));
    }

    @Operation(summary = "Получение завершенных тикетов", description = "Эндпоинт для получения завершенных тикетов")
    @GetMapping(ticket_completed)
    public ResponseEntity<List<TicketForm>> getCompletedTickets() {
        return ResponseEntity.ok(ticketService.getCompletedTickets());
    }

    @Operation(summary = "Взять тикет в обработку", description = "Эндпоинт для взятия тикета в обработку")
    @PostMapping("take/{id}")
    public ResponseEntity<String> takeTicket(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
            ticketService.takeTicket(id, userId);
            TicketForm ticketForm = ticketService.getTicket(id);
            kafkaWorkWithBroker.sendMessage("Тикет взят в обработку с id " + id,
                    EventsNotification.ACCEPTED_TICKET,
                    Math.toIntExact(ticketForm.getAcceptedByUser()),
                    Math.toIntExact(ticketForm.getCreatedByUser()));
            return ResponseEntity.ok("Тикет успешно взят в обработку.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Ошибка при взятии тикета в обработку: " + e.getMessage());
        }
    }

    @Operation(summary = "Добавление комментария к тикету", description = "Эндпоинт для добавления комментария к тикету")
    @PostMapping(ticket_comment_add)
    public ResponseEntity<CommentEntity> addComment(@PathVariable Long ticketId, @RequestBody CommentCreateDto commentDto) {
        try {
            CommentEntity comment = ticketService.addComment(ticketId, commentDto);
            kafkaWorkWithBroker.sendMessage("Добавлен комментарий к тикету с id " + ticketId,
                    EventsNotification.COMMENT_ADDED,
                    Math.toIntExact(commentDto.getCreatedByUser()),
                    Math.toIntExact(ticketService.getTicket(ticketId).getCreatedByUser()));
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}