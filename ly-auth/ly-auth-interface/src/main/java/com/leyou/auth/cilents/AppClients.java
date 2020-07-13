package com.leyou.auth.cilents;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("auth-service")
public interface AppClients {

    /**
     * 生成token的微服务(token的载荷为：id,serverName,targetIdList)
     *
     * @param id     微服务的数据库id
     * @param secret 微服务的密码
     * @return
     */
    @GetMapping("/authorization")
    String authorize(
            @RequestParam("id") Long id, @RequestParam("secret") String secret);

}
