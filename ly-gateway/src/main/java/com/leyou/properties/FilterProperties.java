package com.leyou.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 用来填充配置文件中白名单微服务的信息(例如登录界面等等是必须开放的)
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties {

    private List<String> allowPaths;
}
