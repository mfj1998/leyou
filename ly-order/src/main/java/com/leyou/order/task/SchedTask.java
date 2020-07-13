package com.leyou.order.task;

import com.leyou.auth.cilents.AppClients;
import com.leyou.order.properties.OrderJwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchedTask {

    @Autowired
    private OrderJwtProperties jwtProperties;
    @Autowired
    private AppClients appClients;

    private String token;
    //token的刷新间隔
    private static final long TOKEN_REFRESH_INTERVAL = 86400000L;
    //token刷新失败后重新获取的间隔
    private static final long TOKEN_RETRY_INTERVAL = 10000L;


    @Scheduled(fixedDelay = TOKEN_REFRESH_INTERVAL)
    public void refreshLoadToken() throws InterruptedException {
        while (true) {
            try {
                OrderJwtProperties.AppTokenProperties app = jwtProperties.getApp();
                Long appTokenPropertiesId = app.getId();
                String secret = app.getSecret();
                this.token = appClients.authorize(appTokenPropertiesId, secret);
                log.info("【ORDER】Token生成成功");
                break;
            } catch (Exception e) {
                log.error("【ORDER】Token生成失败");
            }
            Thread.sleep(TOKEN_RETRY_INTERVAL);
        }
    }

    public String getToken() {
        return token;
    }
}
