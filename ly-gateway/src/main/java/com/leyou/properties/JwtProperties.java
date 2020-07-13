package com.leyou.properties;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    private String pubKeyPath;
    private PublicKey publicKey;
    private UserTokenProperties user = new UserTokenProperties();

    /**
     * 存放token的cookie的名字
     */
    @Data
    public class UserTokenProperties {
        private String cookieName;
    }

    private PrivilegeTokenProperties app = new PrivilegeTokenProperties();
    /**
     * 存放token的属性值
     * @throws Exception
     */
    @Data
    public class PrivilegeTokenProperties{
        private Long id;                //服务的id
        private String secret;          //访问服务的密码
        private String headerName;          //用来存放服务token的头
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥失败", e);
            throw new RuntimeException();
        }
    }
}
