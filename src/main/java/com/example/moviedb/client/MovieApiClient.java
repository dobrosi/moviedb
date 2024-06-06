package com.example.moviedb.client;

import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.service.MovieWebClient;
import lombok.Data;
import org.reactivestreams.Publisher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static reactor.core.publisher.Mono.empty;

public interface MovieApiClient {
    @Data
    class ApiMoviesResponse<T extends MovieApiClient.ApiMoviesResponse.ApiMovieItem> {
        @Data
        public static class ApiMovieItem {
            private String id;
            private String title;
            private String year;
        }

        private T[] items;
        private int totalResults;
    }

    String PAGE_PARAMETER = "page";

    String getApiName();

    Flux<MovieDto> getMovies(MovieWebClient movieWebClient, String movieTitle);

    String getApiKeyParameterName();

    String getYear(ApiMoviesResponse.ApiMovieItem movie);

    Mono<Collection<String>> getDirectors(MovieWebClient movieWebClient, Object id);

    default Flux<MovieDto> getMovies(MovieWebClient movieWebClient, String url, Map<String, Object> queryParameters) {
        var queryParametersWithPage = addPageParameter(queryParameters);
        return retrieve(movieWebClient, url, queryParametersWithPage)
                .expand(expandResponse(movieWebClient, url, queryParametersWithPage))
                .flatMap(response -> Flux.fromIterable(convertMovieResponse(movieWebClient, response)));
    }
    
    default <U> Mono<U> retrieve(MovieWebClient movieWebClient, String url, Map<String, Object> queryParameters, Class<U> resultClass) {
        return movieWebClient.retrieve(
                uriBuilder -> uriBuilder
                        .path(url)
                        .queryParams(getQueryParams(queryParameters)),
                resultClass);
    }


    private Mono<ApiMoviesResponse> retrieve(MovieWebClient movieWebClient, String url, Map<String, Object> queryParametersWithPage) {
        return retrieve(movieWebClient, url, queryParametersWithPage, ApiMoviesResponse.class);
    }

    private Function<ApiMoviesResponse, Publisher<ApiMoviesResponse>> expandResponse(MovieWebClient movieWebClient, String url, Map<String, Object> queryParameters) {
        return response -> isEmpty(response.getItems()) ? empty() : retrieve(movieWebClient, url, queryParameters);
    }

    private MultiValueMap<String, String> getQueryParams(Map<String, Object> params) {
        stepPageParameter(params);
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            multiValueMap.put(entry.getKey(), singletonList(entry.getValue().toString()));
        }

        return multiValueMap;
    }

    private void stepPageParameter(Map<String, Object> params) {
        var pageParameter = params.get(PAGE_PARAMETER);
        if (pageParameter != null) {
            ((AtomicInteger) pageParameter).incrementAndGet();
        }
    }

    private Map<String, Object> addPageParameter(Map<String, Object> queryParameters) {
        var map = new HashMap<>(queryParameters);
        map.put(PAGE_PARAMETER, new AtomicInteger(0));
        return Map.copyOf(map);
    }

    private List<MovieDto> convertMovieResponse(MovieWebClient movieWebClient, ApiMoviesResponse response) {
        var items = response.getItems();
        if (isEmpty(items)) {
            return emptyList();
        }
        return stream(items).map(e -> convert(movieWebClient, e)).toList();
    }

    private MovieDto convert(MovieWebClient movieWebClient, ApiMoviesResponse.ApiMovieItem apiMovieItem) {
        try {
            return new MovieDto(
                    apiMovieItem.title,
                    getYear(apiMovieItem),
                    getDirectors(movieWebClient, apiMovieItem.id).toFuture().get(10, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}