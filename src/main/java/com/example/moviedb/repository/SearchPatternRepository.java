package com.example.moviedb.repository;

import com.example.moviedb.model.SearchPattern;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface SearchPatternRepository extends R2dbcRepository<SearchPattern, SearchPattern> {
}
