package com.project.emailservice.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.emailservice.DTO.ConsumerEmailDto;
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

    @KafkaListener(topics = "E-Mails" , groupId = "my-consumer-group")
    public void handleSendEmailEvent(@Payload ConsumerEmailDto emailDto) throws JsonProcessingException {

        String to = emailDto.getEmail();
        String subject = emailDto.getSubject();
        String body = emailDto.getBody();




        //Sending to
        final String fromEmail = "yashvardhann15@gmail.com"; //requires valid gmail id
        final String password = "cqfr cfhb hvcx lmjb"; // correct password for gmail id


        System.out.println("TLSEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
        Session session = Session.getInstance(props, auth);

        EmailUtil.sendEmail(session, to, subject, body);

    }



}
