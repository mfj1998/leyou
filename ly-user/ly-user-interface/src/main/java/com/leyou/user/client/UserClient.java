package com.leyou.user.client;

import com.leyou.pojo.UserAddress;
import com.leyou.pojo.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("user-service")
public interface UserClient {

    /**
     * 根据用户名个密码对用户进行查询
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/query")
    UserDTO queryUser(@RequestParam("username") String username,
                      @RequestParam("password") String password);


    /**
     * 根据用户的id号查询对应的用户手机号
     * @param uid
     * @return
     */
    @GetMapping("/user/phone")
    String queryUserPhoneById(@RequestParam("uid")Long uid);

    /**
     * 根据用户的手机号查询出对应的用户地址信息
     * @return
     */
    @GetMapping("/address")
    List<UserAddress> getAddress();
}
