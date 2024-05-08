package com.example.moviedb.client;

import com.example.moviedb.configuration.ApiProperties;
import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.service.MovieWebClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
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
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static reactor.core.publisher.Mono.empty;

@RequiredArgsConstructor
public abstract class MovieApiClient<T extends MovieApiClient.ApiMoviesResponse<?>> {
    @Data
    public static class ApiMoviesResponse<T extends MovieApiClient.ApiMoviesResponse.ApiMovieItem> {
        @Data
        public static class ApiMovieItem {
            private String id;
            private String title;
            private String year;
        }
        private T[] items;
        private int totalResults;
    }

    private static final String PAGE_PARAMETER = "page";

    public abstract String getApiName();

    public abstract Flux<MovieDto> getMovies(String movieTitle);

    protected abstract String getApiKeyParameterName();

    protected abstract String getYear(ApiMoviesResponse.ApiMovieItem movie);

    protected abstract Mono<Collection<String>> getDirectors(Object id) ;

    private final ApiProperties apiProperties;

    private final MovieWebClient movieWebClient;

    protected Flux<MovieDto> getMovies(String url, Map<String, Object> queryParameters) {
        var queryParametersWithPage = addPageParameter(queryParameters);
        return retrieve(url, queryParametersWithPage)
                .expand(expandResponse(url, queryParametersWithPage))
                .flatMap(response -> Flux.fromIterable(convertMovieResponse(response)));
    }

    protected <U> Mono<U> retrieve(String url, Map<String, Object> queryParameters, Class<U> resultClass) {
        return movieWebClient.retrieve(
                getBaseUrl(),
                uriBuilder -> uriBuilder
                        .path(url)
                        .queryParams(getQueryParams(queryParameters))
                        .queryParam(getApiKeyParameterName(), getApiKey()),
                resultClass);
    }

    private String getBaseUrl() {
        var clients = apiProperties.clients();
        return isEmpty(clients) ? null : clients.get(getApiName()).baseUrl();
    }

    private Mono<T> retrieve(String url, Map<String, Object> queryParametersWithPage) {
        return retrieve(url, queryParametersWithPage, getClazz());
    }

    private Function<T, Publisher<? extends T>> expandResponse(String url, Map<String, Object> queryParameters) {
        return response -> isEmpty(response.getItems()) ? empty() : retrieve(url, queryParameters);
    }

    private LinkedMultiValueMap<String, String> getQueryParams(Map<String, Object> params) {
        stepPageParameter(params);
        return new LinkedMultiValueMap<>(
                params.entrySet().stream().collect(
                        toMap(Map.Entry::getKey, e -> singletonList(e.getValue().toString()))));
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

    private Class<T> getClazz() {
        var superClass = getClass().getGenericSuperclass();
        return  (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    private String getApiKey() {
        return apiProperties.clients().get(getApiName()).key();
    }

    private List<MovieDto> convertMovieResponse(T response) {
        var items = response.getItems();
        if (isEmpty(items)) {
            return emptyList();
        }
        return stream(items).map(this::convert).toList();
    }

    private MovieDto convert(ApiMoviesResponse.ApiMovieItem apiMovieItem) {
        try {
            return new MovieDto(
                    apiMovieItem.title,
                    getYear(apiMovieItem),
                    getDirectors(apiMovieItem.id).toFuture().get(10, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}