package com.maotuanzi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 毛团子AI 后端服务启动类
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
@EnableCaching
@MapperScan("com.maotuanzi.mapper")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
        System.out.println();
        System.out.println("========================================");
        System.out.println("  毛团子AI 后端服务启动成功");
        System.out.println("  API 文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
        System.out.println();
    }
}