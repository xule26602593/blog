package com.blog.domain.dto;

import java.util.List;
import lombok.Data;

@Data
public class LinkCheckResult {
    private int total;
    private int valid;
    private int invalid;
    private List<LinkInfo> links;

    @Data
    public static class LinkInfo {
        private String url;
        private int line;
        private int status;
        private boolean valid;
        private String error;
    }
}
