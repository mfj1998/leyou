package com.leyou.common.utils;

import com.leyou.common.exception.LyException;
import lombok.Getter;
import org.joda.time.DateTime;

/**
 * 异常的日志结果输出
 */
@Getter
public class ExceptionResult {
    private int status;
    private String message;
    private String exceptionTime;

    public ExceptionResult(LyException le) {
        this.status = le.getStatus();
        this.message = le.getMessage();
        this.exceptionTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
    }
}
