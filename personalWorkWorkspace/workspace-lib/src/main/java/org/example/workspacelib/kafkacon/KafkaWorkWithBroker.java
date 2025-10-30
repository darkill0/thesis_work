package org.example.workspacelib.kafkacon;



import org.example.components.kafka.EventsNotification;
import org.example.components.kafka.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Component
public class KafkaWorkWithBroker {

    private final String topic = "notifications";

    private KafkaTemplate kafkaTemplate;

    @Autowired
    public KafkaWorkWithBroker(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message, EventsNotification type, int from, int to) {
        Date now = new Date();
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, now.toString(), new Notification(1, type, message, from, to));
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Send message=[" + message + "]"+" with offset=["+result.getRecordMetadata().offset()+"]");
            }else {
                System.out.println("Unable to send message=[" + message + "]"+" with offset=["+result.getRecordMetadata().offset()+"]");
            }
        });
    }

}
