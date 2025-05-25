package com.project.emailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }

}


//zookeeper-server-start.bat ..\..\config\zookeeper.properties
//
//kafka-server-start.bat ..\..\config\server.properties
//
//kafka-topics.bat --create --topic my-topic --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3
//
//kafka-console-producer.bat --broker-list localhost:9092 --topic my-topic
//
//kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic my-topic --from-beginning