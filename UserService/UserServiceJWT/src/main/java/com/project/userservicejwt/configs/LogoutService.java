package com.project.userservicejwt.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.userservicejwt.Service.JWTService;
import com.project.userservicejwt.Service.RedisService;
import com.project.userservicejwt.Token.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    @Autowired
    private RedisService redisService;
    @Autowired
    private JWTService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = jwtService.extractUserName(token);

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

//            Token storedToken = tokenRepository.findByToken(token)
//                    .orElse(null);


            List<?> rawList = redisService.get("TOKEN_" + email, List.class);
            List<Token> tokens = new ArrayList<>();

            if (rawList != null) {
                ObjectMapper mapper = new ObjectMapper();
                tokens = rawList.stream()
                        .map(item -> mapper.convertValue(item, Token.class))
                        .collect(Collectors.toList());
            }

            for(Token tokenItem : tokens) {
                if(tokenItem.getToken().equals(token)) {
                    tokenItem.setExpired(true);
                    tokenItem.setRevoked(true);
                    tokens.remove(tokenItem);
                    break;
                }
            }

            redisService.remove("TOKEN_" + email);

            if(tokens.size() > 0) {
                redisService.set("TOKEN_" + email , tokens , 60L , TimeUnit.MINUTES);
            }
        }


    }
}
