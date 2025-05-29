package com.project.emailservice.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDto {
    private String email;
    private String subject;
    private String body;
}
