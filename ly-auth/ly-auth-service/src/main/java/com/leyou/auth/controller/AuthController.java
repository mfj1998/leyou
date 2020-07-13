package com.leyou.auth.controller;

import com.leyou.common.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * feign调用user微服务判断账号密码,同时为该用户颁发token凭证
     *
     * @param username
     * @param password
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletResponse response) {
        authService.login(username, password, response);
        return ResponseEntity.ok().build();
    }

    /**
     * 当用户登录跳转到主页面时,钩子函数该方法,得到用户的信息
     * 还需要在redis黑名单中判断该用户是否存在,存在则token无效抛出异常
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request, HttpServletResponse response) {
        UserInfo userInfo = authService.verify(request, response);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 注销用户的接口,需要设置将该token放入到黑名单当中
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }


    /**
     * 生成token的微服务(token的载荷为：id,serverName,targetIdList)
     * @param id        微服务的数据库id
     * @param secret    微服务的密码
     * @return
     */
    @GetMapping("/authorization")
    public ResponseEntity<String> authorize(
            @RequestParam("id") Long id, @RequestParam("secret") String secret) {
        String token = authService.authorize(id,secret);
        return ResponseEntity.ok(token);
    }
}
