package org.example.notifications.listener;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.example.components.kafka.Notification;
import org.example.notifications.config.KafkaConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KafkaListenerNotifications {
    public static Map<Long, List<Notification>>  lists = new HashMap<>();

    final SimpMessagingTemplate simpMessagingTemplate;

    ConsumerFactory<String, Notification> consumerFactory;
    public KafkaConsumer<String, Notification> consumer;
    @Autowired
    public KafkaListenerNotifications(ConsumerFactory<String, Notification> consumerFactory, SimpMessagingTemplate simpMessagingTemplate) {
        this.consumerFactory = consumerFactory;
        Map<String, Object> properties = consumerFactory.getConfigurationProperties();
        consumer = new KafkaConsumer<>(properties);

        this.simpMessagingTemplate = simpMessagingTemplate;
    }
    @PostConstruct
    public void init() {
        getMessages();
    }
    public void getMessages(){
        TopicPartition partition = new TopicPartition("notifications", 0);
        List<TopicPartition> partitions = new ArrayList<>();
        partitions.add(partition);
        consumer.assign(partitions);
        consumer.seekToBeginning(consumer.assignment());

        ConsumerRecords<String, Notification> records = consumer.poll(Duration.ofMinutes(1));

        for(ConsumerRecord<String, Notification> record : records){
            System.out.println("Уведомление: "+record.value());
            if(lists.containsKey((long) record.value().getTo())){
                List<Notification> notifications = lists.get((long)record.value().getTo());
                if(notifications == null){
                    notifications = new ArrayList<>();
                }
                notifications.add(record.value());
                lists.put( (long)record.value().getTo(), notifications);
            }
            else{
                List<Notification> notifications = new ArrayList<>();
                notifications.add(record.value());
                lists.put( (long)record.value().getTo(), notifications);

            }



        }
    }


}
