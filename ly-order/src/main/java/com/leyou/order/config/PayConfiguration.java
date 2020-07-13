package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfigImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayConfiguration {

    /**
     * 对微信实现对象的注入
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "ly.pay.wx")
    public WXPayConfigImpl payConfig() {
        return new WXPayConfigImpl();
    }

    /**
     * 注册WXPay对象
     * @param wxPayConfig
     * @return
     * @throws Exception
     */
    @Bean
    public WXPay wxPay(WXPayConfigImpl wxPayConfig) throws Exception {
        return new WXPay(wxPayConfig);
    }
}
