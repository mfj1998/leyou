package com.leyou.user.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.NumberUtils;
import com.leyou.pojo.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;
import static com.leyou.common.utils.constants.RegexPatterns.PHONE_REGEX;
import static com.leyou.common.utils.constants.RegexPatterns.USERNAME_REGEX;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String phoneStr = "ly:user:code:";


    @Override
    public Boolean checkData(String data, Integer type) {

        if (StringUtils.isBlank(data)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.PARAM_ERROR);
        }

        int count = userMapper.selectCount(user);

        //校验这个用户名是否可用,可用则返回true(即count=0)
        return count == 0;
    }

    @Override
    public void checkCode(String phone) {
        if (StringUtils.isBlank(phone)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        //对phone进行正则校验
        boolean matches = phone.matches(PHONE_REGEX);
        if (!matches) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }


        //生成验证码,并rabbitMQ调用服务的接口
        String code = NumberUtils.generateCode(6);
        Map<String, String> map = new HashMap<>();
        map.put("phone", phone);
        map.put("code", code);
        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME, VERIFY_CODE_KEY, map);

        redisTemplate.opsForValue().set(phoneStr + phone, code, 6000, TimeUnit.SECONDS);

    }

    @Override
    public void register(User user, String code) {
        String redisPhone = phoneStr + user.getPhone();
        if (StringUtils.isBlank(code) || !StringUtils.equals(code, redisTemplate.opsForValue().get(redisPhone))) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        //参数正常加密并添加到数据库,try抓取异常主要是为了防止数据库的关闭等操作
        String encodePass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePass);
        int count = 0;
        try {
            count = userMapper.insertSelective(user);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }

        if (count != 1) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }

        //删除redis中的该值
        if (redisTemplate.opsForValue().get(redisPhone) != null && redisTemplate.getExpire(redisPhone) > 3) {
            redisTemplate.delete(redisPhone);
        }
    }

    @Override
    public UserDTO queryUser(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        if (!username.matches(USERNAME_REGEX) || !password.matches(USERNAME_REGEX)) {
            throw new LyException(ExceptionEnum.PARAM_ERROR);
        }

        User user = new User();
        user.setUsername(username);
        User userInfo = userMapper.selectOne(user);
        if (userInfo == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        boolean matches = passwordEncoder.matches(password, userInfo.getPassword());
        if (!matches) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        return BeanHelper.copyProperties(userInfo,UserDTO.class);
    }

    @Override
    public String queryUserPhoneById(Long uid) {
        User user = userMapper.selectByPrimaryKey(uid);
        if (user == null) {
            throw new LyException(ExceptionEnum.ORDER_PAY_ERROR);
        }
        return user.getPhone();
    }
}
