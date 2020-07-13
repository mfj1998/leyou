package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.Date;

/**
 * 这个主要是用户的一些基本的信息,然后装入到载荷当中
 * @param <T>
 */
@Data
public class Payload<T> {
    private String id;          //token的id
    private T info;             //token主要装的用户的信息
    private Date expiration;    //token失效的时间
}
