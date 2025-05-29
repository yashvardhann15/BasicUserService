package com.project.userservicejwt.Service;

import com.project.userservicejwt.DTO.*;
import com.project.userservicejwt.Exceptions.InvalidOrExpiredOTPException;
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
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private JWTService jwtService;
    private OtpService otpService;
    private KafkaService kafkaService;
    private RedisService redisService;
    private TokenRepository tokenRepository;
    AuthenticationManager authenticationManager;
    @Autowired
    public UserServiceImpl(UserRepository userRepository , RoleRepository roleRepository , BCryptPasswordEncoder bCryptPasswordEncoder , JWTService jwtService , OtpService otpService , RedisService redisService , TokenRepository tokenRepository , AuthenticationManager authenticationManager , KafkaService kafkaService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.redisService = redisService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
        this.kafkaService = kafkaService;
    }

    @Override
    public ResponseEntity<?> registerUser(UserRegisterDTO userRegisterDTO){
        String email = userRegisterDTO.getEmail().toLowerCase();

        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsException();
        }

        String otp = otpService.generateAndStoreOtp(userRegisterDTO);
    String subject = "OTP Verification";
        String body = "This is your OTP for verifying your email:\n" + otp + "\nThis OTP is valid for 5 minutes. \n\n Do not share this OTP with anyone. Kindly ignore this email if you have not requested this OTP.";
        String message = email + " /BREAK/ " + subject + " /BREAK/ " + body;

        kafkaService.sendEmail(message);

        return ResponseEntity.ok("OTP sent to email");
    }

    @Override
    public ResponseEntity<?> registerUserComp(UserRegisterVerifyDTO user) {
        String email = user.getEmail().toLowerCase();
        String OTP = user.getOtp();

        RegisterOtpCacheDTO userDetails = otpService.verifyOtp(email, OTP);

        if(userDetails == null){
            throw new InvalidOrExpiredOTPException();
        }

        String password = userDetails.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(password);

        List<Role> roles = userDetails.getRoles().stream()
                .map(roleName -> roleRepository.findByValue(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toList());

        User newUser = new User(userDetails.getName() , email, encodedPassword, roles);
        userRepository.save(newUser);

        String subject = "Registration Successful.";
        String body = "Welcome " + userDetails.getName()  + ", to Career Connect. \n\n" + "We're happy to inform you that your registration was successful.\n\n" + "You can now log in and start using your account.\n\n" + "If this wasn't you, please ignore this email or get in touch with our support team.\n\n" + "Warm regards,\n" + "The Career Connect Team";
        String msg = email + " /BREAK/ " + subject + " /BREAK/ " + body;
        kafkaService.sendEmail(msg);

        return new ResponseEntity<>(UserProjection.makeProjection(newUser), HttpStatus.OK);
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