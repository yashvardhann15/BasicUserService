package com.project.userservicejwt.DTO;

import com.project.userservicejwt.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOtpCacheDTO extends UserRegisterDTO{
    private String otp;

    public RegisterOtpCacheDTO(UserRegisterDTO user) {
        this.setName(user.getName());
        this.setEmail(user.getEmail());
        this.setPassword(user.getPassword());
        this.setRoles(user.getRoles());
    }

}
