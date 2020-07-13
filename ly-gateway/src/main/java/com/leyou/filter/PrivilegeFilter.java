package com.leyou.filter;

import com.leyou.properties.JwtProperties;
import com.leyou.task.PrivilegeTokenHolder;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

/**
 * 拦截路由转发的接口，然后添加对应的token,无法拦截feign转换的接口，所以还需要在每个微服务上添加feign的
 */
@Slf4j
@Component
public class PrivilegeFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PrivilegeTokenHolder tokenHolder;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    //比第一层拦截器优先级小一级
    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 在Zuul的上下文域的头中添加token
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        currentContext.addZuulRequestHeader(jwtProperties.getApp().getHeaderName(), tokenHolder.getToken());
        return null;
    }
}
