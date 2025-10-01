package com.azatkhaliullin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StatsApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatsApplication.class, args);
    }
}
