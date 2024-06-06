package com.example.moviedb.service;

import com.example.moviedb.client.MovieApiClient;
import com.example.moviedb.configuration.ApiProperties;
import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.model.SearchPattern;
import com.example.moviedb.repository.SearchPatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static org.apache.commons.collections4.MapUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final ApiProperties apiProperties;

    private final Collection<MovieApiClient> movieApiClients;

    private final SearchPatternRepository searchPatternRepository;

    @Cacheable("movie")
    public Flux<MovieDto> getMovies(String movieTitle, String apiName) {
        savePattern(movieTitle, apiName).subscribe();
        return getClient(apiName).getMovies(getMovieWebClient(getApi(apiName)), movieTitle);
    }

    private ApiProperties.Api getApi(String apiName) {
        var clients = apiProperties.clients();
        return isEmpty(clients) ? null : clients.get(apiName);
    }

    private MovieWebClient getMovieWebClient(ApiProperties.Api api) {
        return new MovieWebClient(api);
    }

    private Mono<SearchPattern> savePattern(String movieTitle, String apiName) {
        SearchPattern entity = new SearchPattern(null, null, movieTitle, apiName);
        return searchPatternRepository.save(entity);
    }

    private MovieApiClient getClient(String apiName) {
        return movieApiClients.stream().filter(c -> apiName.equals(c.getApiName())).findFirst().orElseThrow();
    }
}
