package com.project.emailservice.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.emailservice.DTO.EmailDto;
import com.project.emailservice.Utils.EmailUtil;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

@Service
@NoArgsConstructor
public class SendEmailEventConsumers{

//    @Value("${sending.email}")
    final String fromEmail = "yashvardhann15@gmail.com" ;
//    @Value("${sending.email.password}")
    final String password = "emfb beuk nvbs kwog";


    @KafkaListener(topics = "emails" , groupId = "emailService")
    public void handleSendEmailEvent(@Payload String msg) throws JsonProcessingException {

        String[] parts = msg.split(" /BREAK/ ");

        if (parts.length < 3) {
            System.err.println("Invalid email message received: " + msg);
            return;
        }

        String to = parts[0].trim();
        String subject = parts[1].trim();
        String body = parts[2].trim();


        //Sending to

        System.out.println("TLSEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator auth = new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
        Session session = Session.getInstance(props, auth);

        EmailUtil.sendEmail(session, to, subject, body);

    }
}
