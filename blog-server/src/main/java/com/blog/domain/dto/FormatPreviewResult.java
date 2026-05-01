package com.blog.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class FormatPreviewResult {
    private List<Change> changes;
    private int totalChanges;

    @Data
    public static class Change {
        private String rule;
        private String description;
        private int count;
        private List<Detail> details;
    }

    @Data
    public static class Detail {
        private int line;
        private String from;
        private String to;
        private String action;
    }
}
