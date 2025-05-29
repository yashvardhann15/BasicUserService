package com.project.emailservice.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.emailservice.DTO.EmailDto;
import com.project.emailservice.Utils.EmailUtil;
import lombok.NoArgsConstructor;
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

    @KafkaListener(topics = "emails" , groupId = "emailService")
    public void handleSendEmailEvent(@Payload String msg) throws JsonProcessingException {

        String[] parts = msg.split(" /BREAK/ ");

        System.out.println(msg);
        System.out.println();
        for (String part : parts) {
            System.out.println(part);
        }

        if (parts.length < 3) {
            System.err.println("Invalid email message received: " + msg);
            return; // Or log and skip
        }

        String to = parts[0].trim();
        String subject = parts[1].trim();
        String body = parts[2].trim();

        System.out.println("email: " + to);
        System.out.println("otp: " + subject);
        System.out.println("body: " + body);


        //Sending to
        final String fromEmail = "yashvardhann15@gmail.com";
        final String password = "vxjq xqnt btpo xbze";

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
