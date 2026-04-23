package com.blog.domain.dto;

import lombok.Data;

@Data
public class SearchDTO {

    private String keyword;

    private Integer page = 1;

    private Integer size = 10;

    private String sortBy = "relevance"; // relevance 或 time
}
