package com.leyou.user.interceptor;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.config.JwtProperties;
import com.leyou.user.entity.AppInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class TokenInterceptor implements HandlerInterceptor {


    private JwtProperties jwtProperties;

    public TokenInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 解析Token判断,判断请求发送来的权限是否有权限访问该微服务,无则不让过其访问
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            String token = request.getHeader(jwtProperties.getApp().getHeaderName());
            if (StringUtils.isBlank(token)) {
                throw new LyException(ExceptionEnum.TOEKN_MESSAGE_ERROR);
            }

            Payload<AppInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), AppInfo.class);
            AppInfo info = payload.getInfo();
            List<Long> targetList = info.getTargetList();
            Long id = jwtProperties.getApp().getId();
            if (targetList == null && !targetList.contains(id)) {
                return false;
            }
            log.info("【user-service】：访问该微服务成功");
            return true;
        } catch (Exception e) {
            log.error("【user-service】：无法解析该微服务或者无权限访问该微服务");
            return false;
        }
    }
}
