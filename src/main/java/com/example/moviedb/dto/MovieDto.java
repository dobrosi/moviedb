package com.example.moviedb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;

public record MovieDto (
        @JsonProperty("Title")
        String title,

        @JsonProperty("Year")
        String year,

        @JsonProperty("Director")
        Collection<String> directors
) implements Serializable {}

