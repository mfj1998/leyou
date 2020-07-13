package com.leyou.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

@Data
@Configuration
@ConfigurationProperties(prefix = "ly.encoder.crypt")
public class PasswordConfig {

    private String secret;
    private int strength;

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        //利用密匙生成随机的安全码
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        //初始化ScryptPasswordEncoder
        return new BCryptPasswordEncoder(strength,secureRandom);
    }
}
