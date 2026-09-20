package com.echozoo.config.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.echozoo.config")
@MapperScan("com.echozoo.config.mapper")
public class DogConfigWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(DogConfigWebApplication.class, args);
    }
}
