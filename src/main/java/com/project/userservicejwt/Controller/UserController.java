package com.project.userservicejwt.Controller;

import com.project.userservicejwt.DTO.LoginDTO;
import com.project.userservicejwt.DTO.UserDTO;
import com.project.userservicejwt.Projections.UserProjection;
import com.project.userservicejwt.Service.UserService;
import com.project.userservicejwt.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO user) {
        ResponseEntity<?> response = userService.registerUser(user);
        if(response.getStatusCode() == HttpStatus.CONFLICT){
            return new ResponseEntity<>("User with this email already exists" , HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>("User Created Successfully" , HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO user) {
        ResponseEntity<?> response = userService.login(user);
        if(response.getStatusCode() == HttpStatus.OK){
            return new ResponseEntity<>(response.getBody() , HttpStatus.OK);
        }
        return new ResponseEntity<>("BAD CREDENTIALS" , HttpStatus.UNAUTHORIZED);
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

    @GetMapping("/u")
    public ResponseEntity<UserProjection> getUser(@RequestParam String email){
        ResponseEntity<UserProjection> response = userService.getUser(email);
        if(response.getStatusCode() == HttpStatus.OK) return response;
        else return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
