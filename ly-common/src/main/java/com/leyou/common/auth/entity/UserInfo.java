package com.leyou.common.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Long id;            //用户的id信息

    private String username;    //用户的username

    private String role;        //用户的角色名称
}