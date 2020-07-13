package com.leyou.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

import static com.leyou.common.utils.constants.RegexPatterns.PHONE_REGEX;
import static com.leyou.common.utils.constants.RegexPatterns.USERNAME_REGEX;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @Pattern(regexp = USERNAME_REGEX,message = "您输入的用户名格式不正确,4-30")
    private String username;
    @Length(min = 4,max = 30,message = "您输入的验证码的格式不正确")
    private String password;
    @Pattern(regexp = PHONE_REGEX,message = "您输入的手机号的格式不正确")
    private String phone;
    private Date createTime;
    private Date updateTime;
}