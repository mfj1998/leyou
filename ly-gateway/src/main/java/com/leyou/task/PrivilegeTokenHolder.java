package com.leyou.task;

import com.leyou.auth.cilents.AppClients;
import com.leyou.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务，主要是定时去刷新路由微服务的token，防止数据库的数据变化
 */
@Slf4j
@Component
public class PrivilegeTokenHolder {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private AppClients appClients;

    private String token;

    //token的刷新间隔
    private static final long TOKEN_REFRESH_INTERVAL = 86400000L;
    //token刷新失败后重新获取的间隔
    private static final long TOKEN_RETRY_INTERVAL = 10000L;

    /**
     * 定时任务，主要是用来获取和刷新token的
     *
     * @throws InterruptedException
     */
    @Scheduled(fixedDelay = TOKEN_REFRESH_INTERVAL)
    public void loadToken() throws InterruptedException {
        //需要获取到调用feign的两个参数
        while (true) {
            try {

                //调用feign接口，获取到token
                token = appClients.authorize
                        (jwtProperties.getApp().getId(),
                                jwtProperties.getApp().getSecret());
                log.info("【TOKEN】：token刷新成功");
                break;
            } catch (Exception e) {
                log.error("【TOKEN】:token刷新失败");
            }

            //失败则循环重新获取token
            Thread.sleep(TOKEN_RETRY_INTERVAL);
        }
    }

    public String getToken() {
        return this.token;
    }
}
