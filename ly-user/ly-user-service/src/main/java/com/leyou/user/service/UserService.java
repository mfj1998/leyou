package com.leyou.user.service;

import com.leyou.pojo.UserDTO;
import com.leyou.user.entity.User;

public interface UserService {


    /**
     * 数据校验,判断使用的是用户名还是手机登录,并校验数据
     * @param data
     * @param type
     * @return
     */
    Boolean checkData(String data, Integer type);

    /**
     * 根据phone生成对应的验证码,并且存储到redis中
     * @param phone
     */
    void checkCode(String phone);

    /**
     * 用户的注册功能,注意验证码的校验以及密码的加密
     * @param user
     * @param code
     */
    void register(User user,String code);

    /**
     * 根据用户输入的账号密码进行查询的操作
     * @param username
     * @param password
     * @return
     */
    UserDTO queryUser(String username, String password);

    /**
     * 根据用户的id查询用户
     * @param uid
     * @return
     */
    String queryUserPhoneById(Long uid);
}
