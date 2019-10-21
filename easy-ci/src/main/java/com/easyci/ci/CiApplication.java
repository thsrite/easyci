package com.easyci.ci;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.easyci.ci.dao")
public class CiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CiApplication.class, args);
    }
}
