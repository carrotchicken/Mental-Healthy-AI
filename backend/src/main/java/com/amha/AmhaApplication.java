package com.amha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.amha.mapper")
@EnableAsync
public class AmhaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AmhaApplication.class, args);
    }
}
