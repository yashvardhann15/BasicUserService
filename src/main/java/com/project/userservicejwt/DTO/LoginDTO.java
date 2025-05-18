package com.project.userservicejwt.DTO;

import com.project.userservicejwt.models.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    private String email;
    private String password;
    private Role role;
}
