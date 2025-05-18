package com.project.userservicejwt.models;

import com.project.userservicejwt.repositories.UserRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User extends BaseModel {

    private String name;
    private String email;
    private String password;
    @ManyToMany
    private Set<Role> roles;

    public User(String name, String email, String password, List<Role> roles) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = new HashSet<>(roles);
    }

}


/*

  1         M
User ----- Role => M:M
  M         1


 */
