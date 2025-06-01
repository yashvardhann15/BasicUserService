package com.project.userservicejwt.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRegisterDTO {
    private String name;
    private String email;
    private String password;
    List<String> roles;
}
