package com.leyou.order.interceptors;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    private static final String COOKIE_NAME = "LY_TOKEN";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            //获取cookie,并解析token
            String token = CookieUtils.getCookieValue(request, COOKIE_NAME);
            Payload<UserInfo> infoPayload = JwtUtils.getInfoFromToken(token, UserInfo.class);
            UserInfo userInfo = infoPayload.getInfo();

            UserHolder.setUserInfo(userInfo);
            log.info("【ORDER】获取Token成功");
            return true;
        } catch (UnsupportedEncodingException e) {
            log.error("【ORDER】Token获取失败");
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUserInfo();
    }
}
