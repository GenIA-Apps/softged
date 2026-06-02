package com.mediarium.softged;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mediarium.softged")
public class SoftgedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftgedApplication.class, args);
    }

}
