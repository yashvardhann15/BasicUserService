package com.project.userservicejwt.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRegisterVerifyDTO {
    private String email;
    private String otp;
}
