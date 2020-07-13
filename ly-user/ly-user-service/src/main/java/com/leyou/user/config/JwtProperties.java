package com.leyou.user.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

/**
 * 随着Spring的Bean初始化加载生成公私钥
 */

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    //公钥存放地址
    private String pubKeyPath;

    //用户token相关属性
    private UserTokenProperties user = new UserTokenProperties();

    private PublicKey publicKey;

    /**
     * 用户登录注册的token的cookie设置对象
     */
    @Data
    public class UserTokenProperties {
        //token过期时长
        private int expire;
        //存放token的cookie名称
        private String cookieName;
        //存放token的cookie的domain
        private String cookieDomain;
    }

    private AppTokenProperties app = new AppTokenProperties();

    /**
     * 用户访问微服务的token的设置
     */
    @Data
    public class AppTokenProperties {
        //token的过期时长
        private Long id;
        private String secret;
        private String headerName;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}