package com.example.moviedb;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@Slf4j
public class MovieDbApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieDbApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("Application started. Sample link: http://localhost:8080/movies/Rest+in+peace?api=themoviedb");
    }
}
