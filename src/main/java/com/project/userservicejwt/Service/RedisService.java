package com.project.userservicejwt.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.userservicejwt.Projections.UserProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate redisTemplate;

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = (String) redisTemplate.opsForValue().get(key);
            if (json == null) return null;

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("🔍 Redis GET key = " + key + ", value = " + json);

            T user = objectMapper.readValue(json, clazz);

            // ✅ Print fields AFTER deserialization
            if (user instanceof UserProjection up) {
                System.out.println("✅ Redis HIT: " + up.getEmail() + " Roles: " + up.getRoles());
            }

            return user;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public void set(String key, Object o , Long time) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key , json , time , TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
