package com.project.userservicejwt.Service;

import com.project.userservicejwt.DTO.LoginDTO;
import com.project.userservicejwt.DTO.UserDTO;
import com.project.userservicejwt.Exceptions.UserAlreadyExistsException;
import com.project.userservicejwt.Exceptions.UserNotFoundException;
import com.project.userservicejwt.Projections.UserProjection;
import com.project.userservicejwt.models.Role;
import com.project.userservicejwt.models.User;
import com.project.userservicejwt.repositories.RoleRepository;
import com.project.userservicejwt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceJWT implements UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    public UserServiceJWT(UserRepository userRepository , RoleRepository roleRepository , BCryptPasswordEncoder bCryptPasswordEncoder , JWTService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> registerUser(UserDTO userDTO){
        String name = userDTO.getName();
        String email = userDTO.getEmail();
        email.toLowerCase();
        String password = userDTO.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(password);

        List<Role> roles = userDTO.getRoles().stream()
                .map(roleName -> roleRepository.findByValue(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toList());


        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsException();
        }

        User user = new User(name, email, encodedPassword, roles);
        userRepository.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public ResponseEntity<?> login(LoginDTO user){
        String email = user.getEmail();
        email.toLowerCase();
        String password = user.getPassword();
        Optional<User> res = userRepository.findByEmail(email);
        if(res == null) throw new UserNotFoundException();

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if(auth.isAuthenticated()){
            String jwtToken = jwtService.generateToken(email);
            return new ResponseEntity<>(jwtToken, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("BAD CREDENTIALS", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<User>> getAllActiveUsers(){
        List<User> userList = userRepository.findAllActive();
        if (userList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(userList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> addRole(String role) {
        if(role.isEmpty()){
            return new ResponseEntity<>("Role name cannot be empty", HttpStatus.BAD_REQUEST);
        }
        role = role.toUpperCase();
        if(roleRepository.findByValue(role).isPresent()){
            return new ResponseEntity<>("Role already exists" , HttpStatus.CONFLICT);
        }

        Role newRole = new Role();
        newRole.setValue(role);

        Role check = roleRepository.save(newRole);
        if(check == null){
            return new ResponseEntity<>("Cannot add role", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Role added" , HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserProjection> getUser(String email) {
        if(email == "") return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<User> res = userRepository.findByEmail(email);
        if(res == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        else{
            UserProjection result = new UserProjection();
            result = result.makeProjection(res.get());
            return new ResponseEntity<>(result , HttpStatus.OK);
        }
    }
}