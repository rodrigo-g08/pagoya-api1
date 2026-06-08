package com.hampcode.pagoya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PagoyaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PagoyaApplication.class, args);
    }
}
