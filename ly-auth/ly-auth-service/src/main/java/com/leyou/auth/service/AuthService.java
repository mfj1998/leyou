package com.leyou.auth.service;

import com.leyou.common.auth.entity.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {

    /**
     * 登陆的校验,需要用到jwt进行登陆的校验
     * @param username
     * @param password
     */
    void login(String username, String password, HttpServletResponse response);

    /**
     * 根据携带的token凭证获取到用户的信息,并且判断token的剩余时间,从而token刷新
     * @param request
     * @param response
     * @return
     */
    UserInfo verify(HttpServletRequest request, HttpServletResponse response);

    /**
     * 注销用户的接口,需要设置将该token放入到黑名单当中
     * @param request
     * @param response
     * @return
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 对用户访问微服务添加权限
     * @param id
     * @param secret
     * @return
     */
    String authorize(Long id, String secret);
}
