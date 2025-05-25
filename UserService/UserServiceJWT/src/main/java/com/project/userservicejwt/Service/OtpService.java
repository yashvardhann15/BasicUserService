package com.project.userservicejwt.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateAndStoreOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(899999) + 100000);
        redisTemplate.opsForValue().set("OTP_" + email, otp, 5, TimeUnit.MINUTES);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String key = "OTP_" + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key); // cleanup
            return true;
        }
        return false;
    }
}
