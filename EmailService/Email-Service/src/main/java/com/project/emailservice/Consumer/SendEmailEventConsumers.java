package com.project.emailservice.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.emailservice.DTO.SendEmailDto;
import com.project.emailservice.Utils.EmailUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

@Component
public class SendEmailEventConsumers{

    private ObjectMapper mapper;

    public SendEmailEventConsumers(ObjectMapper mapper){
        this.mapper = mapper;
    }
    @KafkaListener(topics = "E-Mails" , groupId = "emailService")
    public void handleSendEmailEvent(String message) throws JsonProcessingException {
        SendEmailDto emailDto = mapper.readValue(message , SendEmailDto.class);

        String to = emailDto.getEmail();
        String subject = emailDto.getSubject();
        String body = emailDto.getBody();




        //Sending to
        final String fromEmail = System.getenv("MAIL"); //requires valid gmail id
        final String password = System.getenv("PASS"); // correct password for gmail id


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
