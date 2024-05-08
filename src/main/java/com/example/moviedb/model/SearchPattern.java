package com.example.moviedb.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("search_pattern")
public record SearchPattern(
        @Id
        Long id,

        @CreatedDate
        LocalDateTime createdDate,

        String movieTitle,

        String api
) {}