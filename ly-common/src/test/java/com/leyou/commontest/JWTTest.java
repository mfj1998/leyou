package com.leyou.commontest;


import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JWTTest {
    private String privateFilePath = "D:\\学习使我快乐\\IO流文件\\privateFile";
    private String publicFilePath = "D:\\学习使我快乐\\IO流文件\\publicFile.pub";

    @Test
    public void testRSA() throws Exception {
        //使用工具类的该方法生成密匙对
        RsaUtils.generateKey(publicFilePath, privateFilePath, "haha", 2048);

        //获取私匙
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        System.out.println(privateKey);

        //获取到公匙
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        System.out.println(publicKey);
    }

    @Test
    public void testJWT() throws Exception {
        //获取私匙
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        //生成token
        String token =
                JwtUtils.generateTokenExpireInMinutes(
                        new UserInfo(1L, "menghaihai", "root"), privateKey, 5);
        System.out.println("token=" + token);

        //获取公匙
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        //解析token
        Payload<UserInfo> fromToken = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);

        System.out.println(fromToken.getId());
        System.out.println(fromToken.getInfo());
        System.out.println(fromToken.getExpiration());
    }
}
