package com.leechao.didi.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
//将指定的controller注解的处理器先经过如下的异常处理器进行处理
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody//最终将结果封装成json数据
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     *- @ControllerAdvice，是Spring3.2提供的新注解,它是一个Controller增强器,
     * 可对controller中被 @RequestMapping注解的方法加一些逻辑处理。最常用的就是异常处理。
     * - 统一异常处理
     *   需要配合@ExceptionHandler使用。当将异常抛到controller时,
     *   可以对异常进行统一处理,规定返回的json格式或是跳转到一个错误页面。
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)//指定处理的异常的类型
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")) {//如果抛出的异常中含有这两个关键字
            String[] split = ex.getMessage().split(" ");//将控制台拿到的信息：Duplicate entry '1001' for key 'employee.idx_username'分割，去掉空格
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误！");
    }

    @ExceptionHandler(CustomException.class)//指定处理的异常的类型
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}