package com.example.moviedb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static reactor.core.scheduler.Schedulers.parallel;

@Service
@RequiredArgsConstructor
public class MovieWebClient {
    public <U> Mono<U> retrieve(
            String baseUrl,
            Function<UriBuilder, UriBuilder> uriFunction,
            Class<U> resultClass) {
        return getWebClient(baseUrl).get()
                .uri(uriBuilder -> uriFunction.apply((uriBuilder)).build())
                .retrieve()
                .bodyToMono(resultClass)
                .subscribeOn(parallel());
    }

    private WebClient getWebClient(String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
