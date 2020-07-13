package com.leyou.config;

import com.leyou.properties.CORSProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 这是一个zuul网关的拦截器，会对所有访问的资源添加一些消息头，从而解决资源跨域访问的问题
 *  SpringMVC的CorsFilter已经帮我们写好了CORS跨域过滤器
 */
@Configuration
@EnableConfigurationProperties(CORSProperties.class)
public class GlobalCORSConfig {

    /**
     * 跨域请求的拦截过滤器
     * @return
     */
    @Bean
    public CorsFilter corsFilter(CORSProperties properties) {
        //1、添加CORS的配置信息
        CorsConfiguration config = new CorsConfiguration();


        //1.1、允许的域
        properties.getAllowedOrigins().forEach(config::addAllowedOrigin);
        //1.2、是否发送cookie信息
        config.setAllowCredentials(properties.getAllowCredentials());
        //1.3、允许的请求
        properties.getAllowedMethods().forEach(config::addAllowedMethod);
        //1.4、允许的头信息
        properties.getAllowedHeaders().forEach(config::addAllowedHeader);
        //1.5、域检测的生命周期
        config.setMaxAge(properties.getMaxAge());


        //2、添加映射路径，拦截一切请求，从而进行判断和增强
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration(properties.getFilterPath(),config);

        //3、返回型的CoresFilter
        return new CorsFilter(configurationSource);
    }
}
