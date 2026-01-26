package com.example.quoraapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class QuoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoraApplication.class, args);
    }

}