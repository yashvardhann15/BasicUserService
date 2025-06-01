package com.project.userservicejwt.Service;

import com.project.userservicejwt.DTO.RegisterOtpCacheDTO;
import com.project.userservicejwt.DTO.UserRegisterDTO;
import com.project.userservicejwt.Exceptions.InvalidOrExpiredOTPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private RedisService redisService;
//    private RedisTemplate<String, Object> redisTemplate;

    public String generateAndStoreOtp(UserRegisterDTO user) {
        String otp = String.valueOf(new Random().nextInt(899999) + 100000);

        RegisterOtpCacheDTO cacheDTO = new RegisterOtpCacheDTO(user);
        cacheDTO.setOtp(otp);

//        redisTemplate.delete("OTP_" + cacheDTO.getEmail());
        redisService.remove("OTP_" + cacheDTO.getEmail());
        redisService.set("OTP_" + cacheDTO.getEmail() , cacheDTO , 5L , TimeUnit.MINUTES);

        System.out.println(redisService.get("OTP_" + cacheDTO.getEmail() , RegisterOtpCacheDTO.class));
//        redisTemplate.opsForValue().set("OTP_" + cacheDTO.getEmail(), cacheDTO, 5, TimeUnit.MINUTES);
        return otp;
    }

    public RegisterOtpCacheDTO verifyOtp(String email, String otp) {
        String key = "OTP_" + email;
        RegisterOtpCacheDTO storedOtp = redisService.get(key , RegisterOtpCacheDTO.class);

        System.out.println(storedOtp);

        if (storedOtp != null && storedOtp.getOtp().equals(otp)) {
            redisService.remove(key);
            return storedOtp;
        }
        return null;
    }
}
