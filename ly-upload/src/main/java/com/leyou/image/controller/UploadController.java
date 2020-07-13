package com.leyou.image.controller;

import com.leyou.image.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 接收上传的图片的方法，需要放回它的url路径用来回显
     *
     * @param
     * @return
     */
    @RequestMapping("/signature")
    public ResponseEntity<Map<String, Object>> getAliSignature() {
        return ResponseEntity.ok(uploadService.getSignature());
    }
}
