package com.blog.domain.vo;

import java.util.List;
import lombok.Data;

@Data
public class KnowledgeGraphVO {

    private List<Node> nodes;

    private List<Link> links;

    @Data
    public static class Node {
        private Long id;
        private String name;
        private Integer count;
    }

    @Data
    public static class Link {
        private Long source;
        private Long target;
        private Integer weight;
    }
}
