package com.example.moviedb.client;

import com.example.moviedb.client.impl.ThemoviedbMovieApiClient;
import com.example.moviedb.configuration.ApiProperties;
import com.example.moviedb.service.MovieWebClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ThemoviedbMovieApiClientTest {
    @InjectMocks
    ThemoviedbMovieApiClient themoviedbMovieApiClient;

    @Mock
    ApiProperties apiProperties;

    @Mock
    MovieWebClient movieWebClient;

    @Test
    void getApiName() {
        assertEquals("themoviedb", themoviedbMovieApiClient.getApiName());
    }

    @Test
    void getMovies() {
        ThemoviedbMovieApiClient.MoviesResponse moviesResponse = new ThemoviedbMovieApiClient.MoviesResponse();
        ThemoviedbMovieApiClient.MoviesResponse emptyResponse = new ThemoviedbMovieApiClient.MoviesResponse();
        ThemoviedbMovieApiClient.MoviesResponse.MovieItem movieItem = new ThemoviedbMovieApiClient.MoviesResponse.MovieItem();
        movieItem.setId("1");
        movieItem.setYear("2000");
        movieItem.setTitle("Jurassic Park");
        moviesResponse.setItems(new ThemoviedbMovieApiClient.MoviesResponse.MovieItem[] {movieItem});

        when(movieWebClient.retrieve(any(), any(), eq(ThemoviedbMovieApiClient.MoviesResponse.class)))
                .thenReturn(
                        Mono.just(moviesResponse),
                        Mono.just(emptyResponse));
        when(movieWebClient.retrieve(any(), any(), eq(ThemoviedbMovieApiClient.CreditsResponse.class)))
                .thenReturn(Mono.just(getCreditsResponse()));

        StepVerifier
                .create(themoviedbMovieApiClient.getMovies(""))
                .expectNextMatches(m -> m.title().equals("Jurassic Park"))
                .expectComplete()
                .verify();

        verify(apiProperties, times(3)).clients();
    }

    @Test
    void getApiKeyParameterName() {
        assertEquals("api_key", themoviedbMovieApiClient.getApiKeyParameterName());
    }

    @Test
    void getYear() {
        MovieApiClient.ApiMoviesResponse.ApiMovieItem movie = mock();

        when(movie.getYear()).thenReturn("2024");

        assertEquals("2024", themoviedbMovieApiClient.getYear(movie));
    }

    @Test
    void getDirectors() {
        ThemoviedbMovieApiClient.CreditsResponse creditsResponse = getCreditsResponse();
        when(movieWebClient.retrieve(any(), any(), any())).thenReturn(Mono.just(creditsResponse));

        themoviedbMovieApiClient
                .getDirectors(1)
                .as(StepVerifier::create)
                .expectNext(List.of("Steven Spielberg", "Stanley Kubrick"))
                .verifyComplete();

        verify(apiProperties).clients();
    }

    private ThemoviedbMovieApiClient.@NotNull CreditsResponse getCreditsResponse() {
        ThemoviedbMovieApiClient.Crew crew1 = new ThemoviedbMovieApiClient.Crew(
                "Steven Spielberg",
                "director");
        ThemoviedbMovieApiClient.Crew crew2 = new ThemoviedbMovieApiClient.Crew(
                "Stanley Kubrick",
                "director");
        return new ThemoviedbMovieApiClient.CreditsResponse(new ThemoviedbMovieApiClient.Crew[]{crew1, crew2});
    }
}
