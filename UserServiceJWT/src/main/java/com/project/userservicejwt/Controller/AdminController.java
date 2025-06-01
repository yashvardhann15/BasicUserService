package com.project.userservicejwt.Controller;

import com.project.userservicejwt.Service.UserService;
import com.project.userservicejwt.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    UserService userService;
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/allusers")
    public ResponseEntity<List<User>> getAllUsers() {
        ResponseEntity<List<User>> users = userService.getAllUsers();
        if(users.getStatusCode() == HttpStatus.NO_CONTENT) return new ResponseEntity<>(null , HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(users.getBody(), HttpStatus.OK);
    }

    @PostMapping("/addrole")
    public ResponseEntity<String> addRole(@RequestParam String value) {
        ResponseEntity<String> response =  userService.addRole(value);

        if(response.getStatusCode() == HttpStatus.OK){
            return new ResponseEntity<>("Role added" , HttpStatus.OK);
        }

        return new ResponseEntity<>(response.getBody() , response.getStatusCode());
    }
}
