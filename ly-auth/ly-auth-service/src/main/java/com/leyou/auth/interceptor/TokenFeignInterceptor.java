package com.leyou.auth.interceptor;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.task.AppTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 使用feign拦截器,拦截feign请求过来的接口,然后添加对应的token
 *
 */

@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class TokenFeignInterceptor implements RequestInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private AppTokenHolder tokenHolder;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = tokenHolder.getToken();
        requestTemplate.header(jwtProperties.getApp().getHeaderName(), token);
    }
}
