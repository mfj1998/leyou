package com.leyou.user.controller;

import com.leyou.pojo.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.pojo.UserAddress;
import com.leyou.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 数据校验,判断使用的是用户名还是手机登录,并校验数据
     * 主要用于前端的正则校验
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    @ApiOperation(value = "校验用户名数据是否可用,如果不存在则代表着可用")
    @ApiResponses({
            @ApiResponse(code = 200, message = "校验结果有效,返回true,该账户可用"),
            @ApiResponse(code = 400, message = "请求参数的错误"),
            @ApiResponse(code = 500, message = "服务器内部的错误"),
    })
    public ResponseEntity<Boolean> checkData(
            @ApiParam(value = "要校验的数据", example = "嗯哼")
            @PathVariable("data") String data,
            @ApiParam(value = "请求的数据类型：1、用户名,2、手机号", example = "1")
            @PathVariable("type") Integer type) {
        Boolean bool = userService.checkData(data, type);
        return ResponseEntity.ok(bool);
    }

    /**
     * 根据phone生成对应的验证码,并且存储到redis中
     *
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public ResponseEntity<Void> checkCode(@RequestParam("phone") String phone) {
        userService.checkCode(phone);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 进行用户的注册的功能,主要验证码的校验;还要对用户的密码进行相应的加密
     * Valid注解主要是为了校验pojo中指定的正则表达式
     *
     * @param user
     * @param code
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            /*@RequestParam("username")String username,
            @RequestParam("password")String password,
            @RequestParam("phone")String phone,*/
            @Valid User user,
            @RequestParam("code") String code) {

        userService.register(user, code);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据用户名个密码对用户进行查询
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/query")
    public ResponseEntity<UserDTO> queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        UserDTO userDTO = userService.queryUser(username, password);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/user/phone")
    public ResponseEntity<String> queryUserPhoneById(@RequestParam("uid")Long uid) {
        String phone = userService.queryUserPhoneById(uid);
        return ResponseEntity.ok(phone);
    }

    /**
     * 根据用户的手机号查询出对应的用户地址信息
     * @return
     */
    @GetMapping("/address")
    public ResponseEntity<List<UserAddress>> getAddress() {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(1L);
        userAddress.setAddressee("艾泽拉斯");
        userAddress.setPhone("666666666");
        userAddress.setProvince("卡里姆多大陆");
        userAddress.setCity("达拉然");
        userAddress.setDistrict("精灵森林");
        userAddress.setStreet("太阳井");
        userAddress.setPostCode("7777");

        List<UserAddress> list = new ArrayList<>();
        list.add(userAddress);
        return ResponseEntity.ok(list);
    }


}
