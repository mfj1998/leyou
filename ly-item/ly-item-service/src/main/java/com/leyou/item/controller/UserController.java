package com.leyou.item.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @RequestMapping("{id}")
    public ResponseEntity<String> testEntity(@PathVariable("id") Integer id) {
        if (id == 1) {
            throw new RuntimeException("id失败");
        }
        return ResponseEntity.status(HttpStatus.OK).body("恭喜");
    }
}
