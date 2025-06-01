package com.project.userservicejwt.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.userservicejwt.Projections.UserProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = (String) redisTemplate.opsForValue().get(key);
            if (json == null) return null;

            ObjectMapper objectMapper = new ObjectMapper();
            T res = objectMapper.readValue(json, clazz);

            return res;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void set(String key, Object o , Long time , TimeUnit timeUnit) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key , json , time , timeUnit);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void remove(String key) {
        redisTemplate.delete(key);
    }

}
