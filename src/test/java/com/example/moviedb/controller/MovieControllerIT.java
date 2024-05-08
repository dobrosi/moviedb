package com.example.moviedb.controller;

import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.repository.SearchPatternRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.util.List;

import static java.lang.System.setProperty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
class MovieControllerIT {
    static List<GenericContainer<?>> containers = List.of(mysql(), redis());

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    SearchPatternRepository searchPatternRepository;

    @ParameterizedTest
    @ValueSource(strings = {"themoviedb", "omdbapi"})
    public void test(String api) {
        assertFalse(webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/movies/{movieTitle}")
                    .queryParam("api", api)
                    .build("Üvegtigris"))
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieDto.class)
                .getResponseBody()
                .collectList()
                .block()
                .isEmpty());

        searchPatternRepository
                .findAll()
                .as(StepVerifier::create)
                .assertNext(n -> {
                    assertNotNull(n.createdDate());
                    assertNotNull(n.id());
                })
                .expectNextCount(0)
                .verifyComplete();
    }

    static GenericContainer<?> mysql() {
        var mysql = new MySQLContainer<>("mysql:latest");
        mysql.start();

        setProperty("spring.r2dbc.url", mysql.getJdbcUrl().replaceFirst("jdbc", "r2dbc"));
        setProperty("spring.r2dbc.username", mysql.getUsername());
        setProperty("spring.r2dbc.password", mysql.getPassword());
        setProperty("spring.r2dbc.pool.enabled", "false");

        setProperty("spring.liquibase.url", mysql.getJdbcUrl());
        setProperty("spring.liquibase.user", mysql.getUsername());
        setProperty("spring.liquibase.password", mysql.getPassword());
        return mysql;
    }

    private static GenericContainer<?> redis() {
        var redis = new RedisContainer(DockerImageName.parse("redis:latest"));
        redis.start();

        setProperty("spring.data.redis.host", redis.getHost());
        setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
        return redis;
    }

    @BeforeEach
    void setUp() {
        searchPatternRepository
                .deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterAll
    public static void tearDown() {
        containers.forEach(GenericContainer::stop);
    }
}
