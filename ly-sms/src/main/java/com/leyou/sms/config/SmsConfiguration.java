package com.leyou.sms.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfiguration {

    @Bean
    public IAcsClient acsClient(SmsProperties properties) {
        DefaultProfile defaultProfile =
                DefaultProfile.getProfile(
                        properties.getRegionID(),
                        properties.getAccessKeyID(),
                        properties.getAccessKeySecret());
        return new DefaultAcsClient(defaultProfile);

    }
}
