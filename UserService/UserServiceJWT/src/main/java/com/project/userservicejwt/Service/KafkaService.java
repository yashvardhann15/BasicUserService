package com.project.userservicejwt.Service;

import com.project.userservicejwt.DTO.EmailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendEmail(String msg) {
    try {
        kafkaTemplate.send("emails", msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
