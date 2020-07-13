package com.leyou.common.exception;

import lombok.Getter;
import org.joda.time.DateTime;

@Getter
public class ExceptionResult {
    private Integer status;
    private String message;
    private String dateTime;

    public ExceptionResult(LyException lyException) {
        this.status = lyException.getStatus();
        this.message = lyException.getMessage();
        this.dateTime = DateTime.now().toString("yyyy-MM-dd HH-mm-ss");
    }
}
