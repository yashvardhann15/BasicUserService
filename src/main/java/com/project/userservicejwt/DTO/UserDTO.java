package com.project.userservicejwt.DTO;

import com.project.userservicejwt.models.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {
    private String name;
    private String email;
    private String password;
    List<String> roles;
}
