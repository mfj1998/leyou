package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringCloudApplication
@EnableZuulProxy
@EnableFeignClients
@EnableScheduling           //定时器任务的
public class LyGateway {
    public static void main(String[] args) {
        SpringApplication.run(LyGateway.class,args);
    }
}
