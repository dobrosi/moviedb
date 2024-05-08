package com.example.moviedb.client.impl;

import com.example.moviedb.client.MovieApiClient;
import com.example.moviedb.configuration.ApiProperties;
import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.service.MovieWebClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;

@Component
public class ThemoviedbMovieApiClient extends MovieApiClient<ThemoviedbMovieApiClient.MoviesResponse> {

    private static final String API_ID = "themoviedb";
    private static final String URL = "/search/movie";
    private static final String MOVIE_URL = "/movie";
    private static final String CREDITS_URL = "/credits";
    private static final String API_KEY_PARAMETER = "api_key";
    private static final String QUERY_PARAMETER = "query";
    private static final String INCLUDE_ADULT_PARAMETER = "include_adult";

    public static class MoviesResponse extends ApiMoviesResponse<MoviesResponse.MovieItem> {
        public static class MovieItem extends MovieApiClient.ApiMoviesResponse.ApiMovieItem {
            @JsonProperty("title")
            private String title;

            @JsonProperty("release_date")
            private String year;
        }

        @JsonProperty("results")
        private MovieItem[] items;

        @JsonProperty("total_results")
        private int totalResults;
    }

    public record Crew (
            String name,
            String job
    ) {}

    public record CreditsResponse (
            @JsonProperty("crew")
            Crew[] crews
    ) {}

    public ThemoviedbMovieApiClient(
            @Autowired ApiProperties apiProperties,
            @Autowired MovieWebClient movieWebClient) {
        super(apiProperties, movieWebClient);
    }

    @Override
    public String getApiName() {
        return API_ID;
    }

    @Override
    public Flux<MovieDto>  getMovies(String movieTitle) {
        return getMovies(URL, Map.of(QUERY_PARAMETER, movieTitle, INCLUDE_ADULT_PARAMETER, true));
    }

    @Override
    public String getApiKeyParameterName() {
        return API_KEY_PARAMETER;
    }

    @Override
    public String getYear(ApiMoviesResponse.ApiMovieItem movie) {
        String year = movie.getYear();
        return year == null ? null : year.split("-")[0];
    }

    @Override
    public Mono<Collection<String>> getDirectors(Object id) {
        return retrieve(format("%s/%s%s", MOVIE_URL, id, CREDITS_URL), emptyMap(), CreditsResponse.class)
                .flatMap(
                        response -> Mono.just(
                                stream(response.crews)
                                        .filter(crew -> "director".equalsIgnoreCase(crew.job))
                                        .map(Crew::name)
                                        .toList()));
    }
}
