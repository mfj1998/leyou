package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

/**
 * 自定义的异常
 */
@Getter
public class LyException extends RuntimeException {
    private int status;

    public LyException(ExceptionEnum em) {
        super(em.getMessage());
        this.status = em.getStatus();
    }

    public LyException(ExceptionEnum em,Throwable throwable) {
        super(em.getMessage(),throwable);
        this.status = em.getStatus();
    }
}
