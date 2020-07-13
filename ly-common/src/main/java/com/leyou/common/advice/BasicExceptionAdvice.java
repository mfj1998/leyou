package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.utils.ExceptionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 拦截所有的类，对其返回值进行处理
 */
@RestControllerAdvice           //相当于异常处理的try,service、mapper、controller层的异常都抛到这里，统一进行抓取
@Slf4j
public class BasicExceptionAdvice {

    /**
     * 自定义的异常拦截器
     * @param ly
     * @return
     */
    @ExceptionHandler(LyException.class)        //相当于异常处理的catch
    public ResponseEntity<ExceptionResult> handleException(LyException ly) {
        int statusCode = ly.getStatus() / 100;
        switch (statusCode) {
            case 1:
                log.info(ly.getMessage());
                break;
            case 2:
                log.info(ly.getMessage());
                break;
            case 3:
                log.info(ly.getMessage());
                break;
            case 4:
                log.error(ly.getMessage());
                break;
            case 5:
                log.error(ly.getMessage());
                break;
        }
        return ResponseEntity.status(ly.getStatus()).body(new ExceptionResult(ly));
    }
}
