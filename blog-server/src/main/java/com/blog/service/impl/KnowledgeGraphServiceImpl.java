package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.ArticleTag;
import com.blog.domain.entity.Tag;
import com.blog.domain.vo.KnowledgeGraphVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.ArticleTagMapper;
import com.blog.repository.mapper.TagMapper;
import com.blog.service.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Cacheable(value = "knowledgeGraph", key = "'graph'")
    public KnowledgeGraphVO getGraph() {
        KnowledgeGraphVO graph = new KnowledgeGraphVO();

        // 获取所有标签
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>());

        // 获取每个标签的文章数量
        Map<Long, Integer> tagArticleCount = new HashMap<>();
        for (Tag tag : tags) {
            Long count = articleTagMapper.selectCount(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, tag.getId()));
            tagArticleCount.put(tag.getId(), count.intValue());
        }

        // 构建节点
        List<KnowledgeGraphVO.Node> nodes = tags.stream()
                .filter(tag -> tagArticleCount.getOrDefault(tag.getId(), 0) > 0)
                .map(tag -> {
                    KnowledgeGraphVO.Node node = new KnowledgeGraphVO.Node();
                    node.setId(tag.getId());
                    node.setName(tag.getName());
                    node.setCount(tagArticleCount.getOrDefault(tag.getId(), 0));
                    return node;
                })
                .collect(Collectors.toList());

        // 构建链接（标签共现关系）
        List<KnowledgeGraphVO.Link> links = new ArrayList<>();
        Map<String, Integer> linkWeight = new HashMap<>();

        // 获取所有已发布文章
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .eq(Article::getDeleted, 0));

        for (Article article : articles) {
            List<ArticleTag> articleTags = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getArticleId, article.getId()));

            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .collect(Collectors.toList());

            // 两两组合标签
            for (int i = 0; i < tagIds.size(); i++) {
                for (int j = i + 1; j < tagIds.size(); j++) {
                    Long id1 = Math.min(tagIds.get(i), tagIds.get(j));
                    Long id2 = Math.max(tagIds.get(i), tagIds.get(j));
                    String key = id1 + "-" + id2;
                    linkWeight.merge(key, 1, Integer::sum);
                }
            }
        }

        // 转换为链接列表
        for (Map.Entry<String, Integer> entry : linkWeight.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Long source = Long.parseLong(parts[0]);
            Long target = Long.parseLong(parts[1]);

            KnowledgeGraphVO.Link link = new KnowledgeGraphVO.Link();
            link.setSource(source);
            link.setTarget(target);
            link.setWeight(entry.getValue());
            links.add(link);
        }

        graph.setNodes(nodes);
        graph.setLinks(links);

        return graph;
    }
}
