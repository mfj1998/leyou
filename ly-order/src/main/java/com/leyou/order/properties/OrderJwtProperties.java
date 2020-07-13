package com.leyou.order.properties;

import com.leyou.common.auth.utils.RsaUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties("ly.jwt")
public class OrderJwtProperties implements InitializingBean {

    private String pubKeyPath;
    private PublicKey publicKey;

    private AppTokenProperties app = new AppTokenProperties();

    @Data
    public class AppTokenProperties{
        private Long id;
        private String secret;
        private String headerName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }

    }

}
