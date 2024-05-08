package com.example.moviedb.controller;

import com.example.moviedb.dto.MovieDto;
import com.example.moviedb.service.MovieService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/{movieTitle}")
    public Flux<MovieDto> getMovies(
            @PathVariable
            @NotEmpty
            @Size(min=1, max=255)
            String movieTitle,

            @RequestParam(name = "api")
            @NotEmpty
            @Size(min=1, max=255)
            String apiName) {
        return  movieService.getMovies(movieTitle, apiName);
    }
}
