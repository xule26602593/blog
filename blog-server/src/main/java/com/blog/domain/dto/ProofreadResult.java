package com.blog.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProofreadResult {
    private List<ProofreadError> errors;
    private String correctedText;

    public ProofreadResult() {}

    public ProofreadResult(List<ProofreadError> errors, String correctedText) {
        this.errors = errors;
        this.correctedText = correctedText;
    }
}
