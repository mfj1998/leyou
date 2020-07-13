package com.leyou.filter;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.properties.FilterProperties;
import com.leyou.properties.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    /**
     * 判断访问的资源路径法是否有权限访问
     *      当该位置返回的true表示要执行下面的run(),返回的false表示不执行run()方法
     *      即白名单不执行run
     * @return
     */
    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String requestURI = request.getRequestURI();

        boolean flag = false;
        for (String allowPath : filterProperties.getAllowPaths()) {
            if  (requestURI.startsWith(allowPath)) {
                flag = true;
                break;
            }
        }
        return !flag;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();


        try {
            //获取、解析token
            String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
            Payload<UserInfo> payload = null;
            try {
                payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
            } catch (Exception e) {
                log.error("TOKEN解析失败");
                throw new LyException(ExceptionEnum.TOEKN_MESSAGE_ERROR);
            }

            UserInfo userInfo = payload.getInfo();
            String role = userInfo.getRole();
            String requestURI = request.getRequestURI();
            String remoteHost = request.getRemoteHost();
            log.info("访问的用户为{},角色为{}，ip为{},请求的资源为{}", userInfo.getUsername(), role, remoteHost, requestURI);
        } catch (Exception e) {
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(403);
            log.error("非法访问，未登录，地址：{}", request.getRemoteHost(), e );
        }
        return null;
    }
}
