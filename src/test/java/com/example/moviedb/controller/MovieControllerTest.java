package com.example.moviedb.controller;

import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.service.MovieService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MovieController.class)
class MovieControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    MovieService movieService;

    @ParameterizedTest
    @ValueSource(strings = {"themoviedb", "omdbapi"})
    public void testGetMovies(String api) {
        Flux<MovieDto> movieFlux = Flux.just(mock(MovieDto.class));
        when(movieService.getMovies(any(), any())).thenReturn(movieFlux);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movies/{movieTitle}")
                        .queryParam("api", api)
                        .build("Üvegtigris"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieDto.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }
}
