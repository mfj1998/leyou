package com.leyou.cart.interceptor;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private static final String COOKIE_NAME = "LY_TOKEN";

    /**
     * 拦截器,拦截用户的Token,解析Token并放入到ThreadLocal中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            //获取用户的token
            String token = CookieUtils.getCookieValue(request, COOKIE_NAME);
            //解析token,并且获取到载荷中的userInfo信息
            Payload<UserInfo> userInfoPayload = JwtUtils.getInfoFromToken(token, UserInfo.class);
            UserInfo userInfo = userInfoPayload.getInfo();

            UserHolder.setUserInfo(userInfo);
            log.info("【购物车】线程绑定用户信息成功");
            return true;
        } catch (UnsupportedEncodingException e) {
            log.error("【购物车】线程绑定用户信息失败");
            return false;
        }
    }

    /**
     * 最终通知,访问结束后将线程中的数据删除即可
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUserInfo();
    }
}
