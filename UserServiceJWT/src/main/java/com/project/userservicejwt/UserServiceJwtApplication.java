package com.project.userservicejwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserServiceJwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceJwtApplication.class, args);
    }

}
