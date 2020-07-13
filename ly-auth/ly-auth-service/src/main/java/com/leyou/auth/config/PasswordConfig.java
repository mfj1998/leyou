package com.leyou.auth.config;

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
    public BCryptPasswordEncoder passwordEncoder() {
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        return new BCryptPasswordEncoder(strength,secureRandom);
    }

}
