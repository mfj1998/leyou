package com.leyou.image.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.image.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UploadService {

    //用来规定上传的文件的格式
    private static final List<String> imageType = Arrays.asList("image/png", "image/jpeg", "image/bmp");

    //阿里云的OSS需要的注入类
    @Autowired
    private OSSProperties prop;

    @Autowired
    private OSS client;

    /**
     * 把图片保存到本地的nginx的html文件夹下
     *
     * @param file
     * @return
     */
    public String uploadImage(MultipartFile file) {
        //1、校验图片的格式
        String contentType = file.getContentType();
        if (!imageType.contains(contentType)) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //2、读取图片
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_FAILURE);
        }
        if (bufferedImage == null) {
            throw new LyException(ExceptionEnum.FILE_IS_NULL);
        }

        //3、保存到本地
        File imgDir = new File("D:\\Tools\\java_tools\\nginx-1.12.2\\html");
        if (imgDir.exists()) {
            //不存在目录则创建目录
            imgDir.mkdir();
        }

        try {
            String imgName = file.getOriginalFilename();
            file.transferTo(new File(imgDir, imgName));      //将图片保存到本地
            return "http://image.leyou.com/" + imgName;
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_SAVE_FAILURE);
        }
    }


    /**
     * 上传到阿里云的业务方法，返回的map中为图片的路径，需要前端显示回显
     * @return
     */
    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.FILE_SAVE_FAILURE);
        }
    }


}
