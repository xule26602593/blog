package com.blog.service.impl.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BusinessException;
import com.blog.domain.entity.PromptTemplate;
import com.blog.repository.mapper.PromptTemplateMapper;
import com.blog.service.ai.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private static final int MAX_TEMPLATE_LENGTH = 2000;
    private static final Pattern DANGEROUS_PATTERNS = Pattern.compile(
        "(?i)(ignore|忽略|disregard).*(instruction|指令|prompt|提示|previous|之前)",
        Pattern.MULTILINE
    );

    private final PromptTemplateMapper promptTemplateMapper;

    @Override
    @Cacheable(value = "ai:prompt", key = "#templateKey")
    public PromptTemplate getByKey(String templateKey) {
        return promptTemplateMapper.selectOne(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getTemplateKey, templateKey)
                .eq(PromptTemplate::getStatus, 1)
        );
    }

    @Override
    public List<PromptTemplate> listByCategory(String category) {
        return promptTemplateMapper.selectList(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(StringUtils.hasText(category), PromptTemplate::getCategory, category)
                .eq(PromptTemplate::getStatus, 1)
                .orderByAsc(PromptTemplate::getId)
        );
    }

    @Override
    public List<PromptTemplate> listAll() {
        return promptTemplateMapper.selectList(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getStatus, 1)
                .orderByAsc(PromptTemplate::getId)
        );
    }

    @Override
    @CacheEvict(value = "ai:prompt", allEntries = true)
    public void save(PromptTemplate template) {
        validateTemplate(template);
        promptTemplateMapper.insert(template);
    }

    @Override
    @CacheEvict(value = "ai:prompt", allEntries = true)
    public void updateById(PromptTemplate template) {
        validateTemplate(template);
        promptTemplateMapper.updateById(template);
    }

    @Override
    @CacheEvict(value = "ai:prompt", allEntries = true)
    public void removeById(Long id) {
        promptTemplateMapper.deleteById(id);
    }

    @Override
    public void validateTemplate(PromptTemplate template) {
        // 1. 长度验证
        if (template.getSystemPrompt() != null &&
            template.getSystemPrompt().length() > MAX_TEMPLATE_LENGTH) {
            throw new BusinessException("系统提示词超过最大长度限制（" + MAX_TEMPLATE_LENGTH + "字）");
        }
        if (template.getUserTemplate() != null &&
            template.getUserTemplate().length() > MAX_TEMPLATE_LENGTH) {
            throw new BusinessException("用户模板超过最大长度限制（" + MAX_TEMPLATE_LENGTH + "字）");
        }

        // 2. 危险模式检测
        String content = template.getSystemPrompt() + " " + template.getUserTemplate();
        if (DANGEROUS_PATTERNS.matcher(content).find()) {
            throw new BusinessException("模板包含可疑指令模式，请检查内容");
        }

        // 3. 模板键名验证
        if (template.getTemplateKey() != null &&
            !template.getTemplateKey().matches("^[a-z][a-z0-9_]*$")) {
            throw new BusinessException("模板键名仅允许小写字母、数字和下划线，且必须以字母开头");
        }
    }
}
