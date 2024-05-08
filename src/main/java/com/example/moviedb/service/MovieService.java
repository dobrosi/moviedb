package com.example.moviedb.service;

import com.example.moviedb.client.MovieApiClient;
import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.model.SearchPattern;
import com.example.moviedb.repository.SearchPatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final Collection<MovieApiClient<?>> movieApiClients;

    private final SearchPatternRepository searchPatternRepository;

    @Cacheable("movie")
    public Flux<MovieDto> getMovies(String movieTitle, String apiName) {
        savePattern(movieTitle, apiName).subscribe();
        return getClient(apiName).getMovies(movieTitle);
    }

    private Mono<SearchPattern> savePattern(String movieTitle, String apiName) {
        SearchPattern entity = new SearchPattern(null, null, movieTitle, apiName);
        return searchPatternRepository.save(entity);
    }

    private MovieApiClient<?> getClient(String apiName) {
        return movieApiClients.stream().filter(c -> apiName.equals(c.getApiName())).findFirst().orElseThrow();
    }
}
