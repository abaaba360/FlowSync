package edu.ustb.flowsync;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author:shaowenle
 * @Data:2026/7/17,14:55
 */
@SpringBootApplication
@MapperScan("edu.ustb.flowsync.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}