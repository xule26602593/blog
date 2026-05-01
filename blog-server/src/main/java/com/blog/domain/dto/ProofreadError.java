package com.blog.domain.dto;

import lombok.Data;

@Data
public class ProofreadError {
    private String type;
    private String original;
    private String suggestion;
    private String position;

    public ProofreadError() {}

    public ProofreadError(String type, String original, String suggestion, String position) {
        this.type = type;
        this.original = original;
        this.suggestion = suggestion;
        this.position = position;
    }
}
