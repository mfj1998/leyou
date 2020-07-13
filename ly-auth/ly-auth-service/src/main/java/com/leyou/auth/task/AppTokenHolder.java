package com.leyou.auth.task;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.AppInfo;
import com.leyou.auth.mapper.AuthMapper;
import com.leyou.common.auth.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * auth服务自己给自己颁发Token供外部访问
 */
@Slf4j
@Component
public class AppTokenHolder {

    //token刷新间隔
    private static final long TOKEN_REFRESH_INTERVAL = 86400000L;
    //token获取失败后重试的间隔
    private static final long TOKEN_RETRY_INTERVAL = 10000L;
    private String token;

    @Autowired
    private AuthMapper authMapper;
    @Autowired
    JwtProperties jwtProperties;

    //定时任务
    @Scheduled(fixedDelay = TOKEN_REFRESH_INTERVAL)
    public void loadToken() throws InterruptedException {
        while (true) {
            try {
                //荷载对象的数据填充
                Long id = jwtProperties.getApp().getId();
                List<Long> targetIdList = authMapper.queryTargetIdList(id);
                String secret = jwtProperties.getApp().getSecret();
                AppInfo appInfo = new AppInfo(id, secret, targetIdList);
                //生成Token
                token = JwtUtils.generateTokenExpireInMinutes(appInfo, jwtProperties.getPrivateKey(), jwtProperties.getApp().getExpire());
                log.info("token生成成功");
                break;
            } catch (Exception e) {
                log.error("token生成失败");
            }
            //Token获取失败，休眠该段时间继续获取
            Thread.sleep(TOKEN_RETRY_INTERVAL);
        }
    }

    public String getToken() {
        return token;
    }
}
