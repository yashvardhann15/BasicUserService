package com.project.emailservice.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumerEmailDto {
    private String email;
    private String subject;
    private String body;
}
