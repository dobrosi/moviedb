package com.example.moviedb.service;

import com.example.moviedb.configuration.ApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static reactor.core.scheduler.Schedulers.parallel;

@RequiredArgsConstructor
public class MovieWebClient {
    private final ApiProperties.Api api;

    public <U> Mono<U> retrieve(
            Function<UriBuilder, UriBuilder> uriFunction,
            Class<U> resultClass) {
        return getWebClient(api.baseUrl()).get()
                .uri(uriBuilder -> uriFunction.apply((uriBuilder)).build())
                .retrieve()
                .bodyToMono(resultClass)
                .subscribeOn(parallel());
    }

    private WebClient getWebClient(String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
