package com.example.moviedb.client.impl;

import com.example.moviedb.client.MovieApiClient;
import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.service.MovieWebClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.stream;

@Service
public class OmdbapiMovieApiClient implements MovieApiClient {

    private static final String API_ID = "omdbapi";
    private static final String URL = "/";
    private static final String API_KEY_PARAMETER = "apikey";
    private static final String SEARCH_PARAMETER = "s";
    private static final String DIRECTOR_DELIMITER = ", ";

    public static class MoviesResponse extends ApiMoviesResponse<MoviesResponse.MovieItem> {
        public static class MovieItem extends ApiMovieItem {
            @JsonProperty("imdbID")
            public String id;

            @JsonProperty("Title")
            public String title;

            @JsonProperty("Year")
            public String year;
        }

        @JsonProperty("Search")
        public MovieItem[] items;
    }

    public record MovieDetailsResponse(
            @JsonProperty("Director")
            String director
    ) {}

    @Override
    public String getApiName() {
        return API_ID;
    }

    @Override
    public Flux<MovieDto> getMovies(MovieWebClient movieWebClient, String movieTitle) {
        return getMovies(movieWebClient, URL, Map.of(SEARCH_PARAMETER, movieTitle));
    }

    @Override
    public String getApiKeyParameterName() {
        return API_KEY_PARAMETER;
    }

    @Override
    public String getYear(ApiMoviesResponse.ApiMovieItem movie) {
        return movie.getYear();
    }

    @Override
    public Mono<Collection<String>> getDirectors(MovieWebClient movieWebClient, Object id) {
        return retrieve(movieWebClient, URL, Map.of("i", id), MovieDetailsResponse.class)
                .map(response -> stream(response.director().split(DIRECTOR_DELIMITER)).toList());
    }
}
