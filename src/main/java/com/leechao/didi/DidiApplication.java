package com.leechao.didi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
/*
SpringBootApplication 上使用@ServletComponentScan 注解后
Servlet可以直接通过@WebServlet注解自动注册
Filter可以直接通过@WebFilter注解自动注册
Listener可以直接通过@WebListener 注解自动注册
 */
public class DidiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DidiApplication.class,args);
        log.info("项目启动成功！！！");//通过@Slf4j注解生成info()的日志在控制台上
    }
}
