package com.example.moviedb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@ConfigurationPropertiesScan
@EnableR2dbcAuditing
@EnableR2dbcRepositories
@EnableCaching
@Slf4j
public class MovieDbApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieDbApplication.class, args);
    }
}
