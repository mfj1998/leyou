package com.leyou.order.interceptors;

import com.leyou.order.properties.OrderJwtProperties;
import com.leyou.order.task.SchedTask;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableConfigurationProperties(OrderJwtProperties.class)
public class FeignInterceptor implements RequestInterceptor {

    @Autowired
    OrderJwtProperties orderJwtProperties;
    @Autowired
    private SchedTask schedTask;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String cookieName = orderJwtProperties.getApp().getHeaderName();
        requestTemplate.header(cookieName,schedTask.getToken());
    }
}
