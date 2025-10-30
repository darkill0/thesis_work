package org.example.notifications.controllers;

import lombok.AllArgsConstructor;
import org.example.components.kafka.Notification;
import org.example.notifications.listener.KafkaListenerNotifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class NotificationController {

    final SimpMessagingTemplate messagingTemplate;

    public NotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/api/test/{id}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable long id) {
        List<Notification> result = KafkaListenerNotifications.lists.values().stream()
                .flatMap(List::stream)
                .filter(notification -> notification.getFrom() == id || notification.getTo() == id)
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/notify")
    public ResponseEntity<String> notifyUser(@RequestBody Notification notification) {
        messagingTemplate.convertAndSend("/topic/messages", notification);
        return ResponseEntity.ok("Уведомление отправлено");
    }


    @MessageMapping("/test")
    @SendTo("/topic/messages")
    public Notification receiveNotification(Notification notification) {
        System.out.println("Получено сообщение: " + notification.getMessage());
        return notification;
    }

}
