package com.blog.service.ai;

import com.blog.domain.entity.PromptTemplate;

import java.util.List;

public interface PromptTemplateService {

    PromptTemplate getByKey(String templateKey);

    List<PromptTemplate> listByCategory(String category);

    List<PromptTemplate> listAll();

    void save(PromptTemplate template);

    void updateById(PromptTemplate template);

    void removeById(Long id);

    void validateTemplate(PromptTemplate template);
}
