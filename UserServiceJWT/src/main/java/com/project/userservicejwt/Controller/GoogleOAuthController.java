package com.project.userservicejwt.Controller;

import com.project.userservicejwt.DTO.UserRegisterDTO;
import com.project.userservicejwt.Service.JWTService;
import com.project.userservicejwt.Service.MyUserDetailsService;
import com.project.userservicejwt.Service.UserService;
import com.project.userservicejwt.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/auth/google")
@Slf4j
public class GoogleOAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MyUserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code){
        try{
            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret" , clientSecret);
            params.add("redirect_uri" , "https://developers.google.com/oauthplayground");
            params.add("grant_type" , "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String , String>> request = new HttpEntity<>(params , headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint , request, Map.class);

            String accessToken = (String) tokenResponse.getBody().get("id_token");
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + accessToken;

            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            if(userInfoResponse.getStatusCode() == HttpStatus.OK){
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                UserDetails userDetails;
                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                }
                catch (UsernameNotFoundException ex) {
                    UserRegisterDTO user = new UserRegisterDTO();
                    user.setEmail(email);
                    user.setName(email.substring(0, email.indexOf("@")));
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user.setRoles(Arrays.asList("USER"));

                    userService.registerUser(user);

//                    userDetails = userDetailsService.loadUserByUsername(email);
                }

//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                SecurityContextHolder.getContext().setAuthentication(authentication);

                String jwtToken = jwtService.generateToken(email);
                return new ResponseEntity<>(jwtToken, HttpStatus.OK);

//                return ResponseEntity.status(HttpStatus.OK).build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

//https://www.googleapis.com/auth/userinfo.email
//https://accounts.gogle.com/o/oauth2/auth? (frontend api)