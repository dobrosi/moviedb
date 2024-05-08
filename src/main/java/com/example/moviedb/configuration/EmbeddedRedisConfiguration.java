package com.example.moviedb.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(value = "spring.data.redis.host", havingValue = "embedded", matchIfMissing = true)
public class EmbeddedRedisConfiguration {

    private RedisServer redisServer;

    @PostConstruct
    public void postConstruct() throws IOException {
        (redisServer = new RedisServer()).start();
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        redisServer.stop();
    }
}
