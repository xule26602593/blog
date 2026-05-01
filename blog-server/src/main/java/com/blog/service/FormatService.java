package com.blog.service;

import com.blog.domain.dto.FormatPreviewResult;
import com.blog.domain.dto.LinkCheckResult;
import com.blog.domain.entity.FormatRule;
import java.util.List;

public interface FormatService {

    FormatPreviewResult preview(String content, List<String> ruleKeys);

    String apply(String content, List<String> ruleKeys);

    List<FormatRule> getRules();

    void updateRuleStatus(Long id, Integer status);

    LinkCheckResult checkLinks(String content);
}
