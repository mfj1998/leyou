package com.leyou.auth.service.impl;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.AppInfo;
import com.leyou.auth.entity.ApplicationInfo;
import com.leyou.auth.mapper.AuthMapper;
import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.pojo.UserDTO;
import com.leyou.user.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserClient userClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AuthMapper authMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final static String USER_ROLE = "role_user";

    @Override
    public void login(String username, String password, HttpServletResponse response) {
        /*
            TODO：
                 1、请求到达service层；
                 2、需要先调用远程接口,判断账号密码是否正确
                 3、存在则，正确则调用JwtUtils生成公钥私钥
                 4、将公钥放入到cookie当中,然后返回结果
         */
        UserDTO userDTO = null;
        try {
            userDTO = userClient.queryUser(username, password);
        } catch (Exception e) {
            log.error("用户登录失败,请检查账号密码");
            throw new LyException(ExceptionEnum.PARAM_ERROR);
        }
        if (userDTO == null) {
            log.error("用户登录失败,请检查账号密码");
            throw new LyException(ExceptionEnum.PARAM_ERROR);
        }

        //分配权和角色,然后生成token,然后生成token并且把用户信息写入到token中
        try {
            UserInfo userInfo = new UserInfo(userDTO.getId(), userDTO.getUsername(), USER_ROLE);
            String token = JwtUtils.generateTokenExpireInMinutes
                    (userInfo, jwtProperties.getPrivateKey(), jwtProperties.getUser().getExpire());


            //将生成的token装入到cookie当中
            CookieUtils.newBuilder()
                    .response(response)         //用于写cookie
                    .httpOnly(true)             //保证安全防止XSS攻击,不允许js操作cookie
                    .domain(jwtProperties.getUser().getCookieDomain()) //设置admin
                    .name(jwtProperties.getUser().getCookieName())     //设置cookie名字和值
                    .value(token)
                    .build();
        } catch (Exception e) {
            log.error("账号登录失败");
            throw new LyException(ExceptionEnum.PARAM_ERROR);
        }

    }

    @Override
    public UserInfo verify(HttpServletRequest request, HttpServletResponse response) {
        /*
            TODO:
                1、先获取到token凭证
                2、解析token凭证的信息
                3、从解析中获取到失效的时间,然后判断
                4、若已经失效,则刷新token
         */
        //1、先获取到凭证
        String token =
                CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());

        //2、工具类解析token的信息,并封装到UserInfo中返回
        Payload<UserInfo> payload = null;
        try {
            payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            log.error("【信息获取】：token解析失败");
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }

        if (payload.getInfo() == null) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }


        //从发redis中获取token信息的id,存在则抛出异常
        String id = payload.getId();
        Boolean idKey = redisTemplate.hasKey(id);
        if (idKey != null && idKey) {
            deleteCookie(response);
            log.info("【信息获取】：该token已经失效");
            throw new LyException(ExceptionEnum.USER_TOKEN_ERROR);
        }

        //3、判断token是否过了刷新的时间,然后对其进行刷新操作
        //获取到过期时间
        Date payTime = payload.getExpiration();

        //获取刷新的时间,过期时间 - 最小刷新时间
        DateTime dateTime =
                new DateTime(payTime.getTime()).minusMinutes(jwtProperties.getUser().getMinRefreshInterval());
        //判断当前时间是否在刷新时间之后
        if (dateTime.isBefore(System.currentTimeMillis())) {
            CookieUtils.newBuilder()
                    .response(response)
                    .httpOnly(true)
                    .domain(jwtProperties.getUser().getCookieDomain())
                    .name(jwtProperties.getUser().getCookieName())
                    .value(token)
                    .build();
        }
        return payload.getInfo();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        /*
            TODO:
                1、前端点击注销后先获取到token的信息,获取到token的失效时间
                2、获取到失效的时间,失效时间大于5s写入到redis中,删除cookie;写入redis相当于一个id黑名单
         */
        String token =
                CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
        Payload<UserInfo> payLoad = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);

        //过期时间 - 当前时间 = 时间差, 时间差> +5则写入到redis
        String id = payLoad.getId();
        Date payLoadTime = payLoad.getExpiration();
        Long time = payLoadTime.getTime() - System.currentTimeMillis();
        if (time > 5) {
            redisTemplate.opsForValue().set(id, "", time, TimeUnit.MILLISECONDS);
        }
        deleteCookie(response);
    }

    @Override
    public String authorize(Long id, String secret) {
        //因为返回的为token需要将Appinfo这个对象的数据填充，三个属性id、serverName、targetList
        ApplicationInfo applicationInfo = authMapper.selectByPrimaryKey(id);
        if (applicationInfo == null) {
            throw new LyException(ExceptionEnum.SECURITY_ERROR);
        }
        if (!passwordEncoder.matches(secret, applicationInfo.getSecret())) {
            log.error("【TOKEN】：查询token的密码不能为空");
            throw new LyException(ExceptionEnum.PARAM_ERROR);
        }

        //查询中间表targetist得id集合
        List<Long> list = authMapper.queryTargetIdList(id);
        if (CollectionUtils.isEmpty(list)) {
            log.error("【TOKEN】：查询到的权限不能为空");
            throw new LyException(ExceptionEnum.SECURITY_ERROR);
        }

        AppInfo appInfo = new AppInfo();
        appInfo.setId(id);
        appInfo.setServiceName(applicationInfo.getServiceName());
        appInfo.setTargetList(list);

        //生成token
        try {
            String token = JwtUtils.generateTokenExpireInMinutes(appInfo,
                    jwtProperties.getPrivateKey(),
                    jwtProperties.getApp().getExpire());

            return token;
        } catch (Exception e) {
            log.error("【TOKEN】token信息解析失败");
            throw new LyException(ExceptionEnum.TOEKN_MESSAGE_ERROR);
        }
    }

    /**
     * 删除cookie
     *
     * @param response
     */
    public void deleteCookie(HttpServletResponse response) {
        //删除cookie
        Cookie cookie = new Cookie(jwtProperties.getUser().getCookieName(), "");
        cookie.setDomain(jwtProperties.getUser().getCookieDomain());
        cookie.setMaxAge(0);        //设置为0则删除cookie
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
