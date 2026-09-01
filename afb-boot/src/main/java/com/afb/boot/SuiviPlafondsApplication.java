package com.afb.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.afb")
@EntityScan(basePackages = "com.afb")
@EnableJpaRepositories(basePackages = "com.afb")
public class SuiviPlafondsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuiviPlafondsApplication.class, args);
    }
}