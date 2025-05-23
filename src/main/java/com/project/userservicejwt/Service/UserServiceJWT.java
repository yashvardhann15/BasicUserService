package com.project.userservicejwt.Service;

import com.project.userservicejwt.DTO.LoginDTO;
import com.project.userservicejwt.DTO.UserRegisterDTO;
import com.project.userservicejwt.Exceptions.UserAlreadyExistsException;
import com.project.userservicejwt.Exceptions.UserNotFoundException;
import com.project.userservicejwt.Projections.UserProjection;
import com.project.userservicejwt.Token.Token;
import com.project.userservicejwt.Token.TokenRepository;
import com.project.userservicejwt.Token.TokenType;
import com.project.userservicejwt.models.Role;
import com.project.userservicejwt.models.User;
import com.project.userservicejwt.repositories.RoleRepository;
import com.project.userservicejwt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    TokenRepository tokenRepository;


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
    public ResponseEntity<?> registerUser(UserRegisterDTO userRegisterDTO){
        String name = userRegisterDTO.getName();
        String email = userRegisterDTO.getEmail();
        email.toLowerCase();
        String password = userRegisterDTO.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(password);

        List<Role> roles = userRegisterDTO.getRoles().stream()
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

            Token token = Token.builder()
                    .user(res.get())
                    .token(jwtToken)
                    .tokenType(TokenType.BEARER)
                    .expired(false)
                    .revoked(false)
                    .build();

            revokeAllUserTokens(res.get());

            tokenRepository.save(token);

            return new ResponseEntity<>(jwtToken, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("BAD CREDENTIALS", HttpStatus.UNAUTHORIZED);
        }
    }


    private void revokeAllUserTokens(User user){
        List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(user.getEmail());

        if(validUserTokens.isEmpty()){
            return;
        }

        for(int i = 0 ; i < validUserTokens.size() ; i++){
            Token token = validUserTokens.get(i);
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
        }
    }


    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

//        redisTemplate.opsForValue().set("healthCheckKey", "connected");
//        String value = redisTemplate.opsForValue().get("salary");
//
//        System.out.println("✅ Redis is working. Value retrieved: " + value);
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
        UserProjection user = redisService.get("user:" + email , UserProjection.class);

        if(user != null){
            System.out.println("val : " + user);
            return new ResponseEntity<>(user , HttpStatus.OK);
        }

        Optional<User> res = userRepository.findByEmail(email);
        if (!res.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else{
            UserProjection result = new UserProjection();
            result = result.makeProjection(res.get());
            redisService.set("user:" + email , result , 100L);
            return new ResponseEntity<>(result , HttpStatus.OK);
        }
    }
}