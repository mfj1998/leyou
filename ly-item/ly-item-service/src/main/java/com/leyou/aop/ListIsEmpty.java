package com.leyou.aop;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Aspect
@Component
public class ListIsEmpty {
    //切入点,切入service层
    @Pointcut("execution(* com.leyou.item.service.impl.*.*(..))")
    public void pc() {
    }

    @Around("pc()")
    public Object aroundListIsEmpty(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Object proceed = null;
        try {
            proceed = joinPoint.proceed(args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


        if (proceed != null) {
            Class<?> aClass = proceed.getClass();
            //判断类型
            if (List.class.isAssignableFrom(aClass)) {
                List list = (List) proceed;
                if (CollectionUtils.isEmpty(list)) {
                    throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
                }
            }
        }
        return proceed;
    }
}
