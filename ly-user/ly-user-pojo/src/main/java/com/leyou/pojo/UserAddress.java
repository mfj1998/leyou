package com.leyou.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAddress {

    private Long id;
    private String addressee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String street;
    private String postCode;
}
