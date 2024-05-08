package com.example.moviedb.client;

import com.example.moviedb.client.impl.OmdbapiMovieApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OmdbapiMovieApiClientTest {
    @InjectMocks
    OmdbapiMovieApiClient omdbapiAbstractMovieApiClient;

    @Mock
    MovieApiClient.ApiMoviesResponse.ApiMovieItem movie;

    @Test
    void getApiName() {
        assertEquals("omdbapi", omdbapiAbstractMovieApiClient.getApiName());
    }

    @Test
    void getMovies() {
        // FIXME
    }

    @Test
    void getApiKeyParameterName() {
        assertEquals("apikey", omdbapiAbstractMovieApiClient.getApiKeyParameterName());
    }

    @Test
    void getYear() {
        when(movie.getYear()).thenReturn("2024");

        assertEquals("2024", omdbapiAbstractMovieApiClient.getYear(movie));
    }

    @Test
    void getDirectors() {
        // FIXME
    }
}