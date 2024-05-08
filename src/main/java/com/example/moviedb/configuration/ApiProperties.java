package com.example.moviedb.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "api")
public record ApiProperties(Map<String, Api> clients) {
    public record Api(String key, String baseUrl) {}
}