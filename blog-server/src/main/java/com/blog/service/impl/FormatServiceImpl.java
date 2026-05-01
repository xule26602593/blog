package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.dto.FormatPreviewResult;
import com.blog.domain.dto.LinkCheckResult;
import com.blog.domain.entity.FormatRule;
import com.blog.repository.mapper.FormatRuleMapper;
import com.blog.service.FormatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormatServiceImpl implements FormatService {

    private final FormatRuleMapper ruleMapper;
    private final ObjectMapper objectMapper;

    @Override
    public FormatPreviewResult preview(String content, List<String> ruleKeys) {
        FormatPreviewResult result = new FormatPreviewResult();
        result.setChanges(new ArrayList<>());

        List<FormatRule> rules = getEnabledRules(ruleKeys);

        for (FormatRule rule : rules) {
            FormatPreviewResult.Change change = applyRulePreview(content, rule);
            if (change != null && change.getCount() > 0) {
                result.getChanges().add(change);
            }
        }

        result.setTotalChanges(result.getChanges().stream()
                .mapToInt(FormatPreviewResult.Change::getCount)
                .sum());

        return result;
    }

    @Override
    public String apply(String content, List<String> ruleKeys) {
        List<FormatRule> rules = getEnabledRules(ruleKeys);

        String result = content;
        for (FormatRule rule : rules) {
            result = applyRule(result, rule);
        }

        return result;
    }

    @Override
    public List<FormatRule> getRules() {
        return ruleMapper.selectList(new LambdaQueryWrapper<FormatRule>().orderByAsc(FormatRule::getPriority));
    }

    @Override
    public void updateRuleStatus(Long id, Integer status) {
        FormatRule rule = new FormatRule();
        rule.setId(id);
        rule.setStatus(status);
        ruleMapper.updateById(rule);
    }

    @Override
    public LinkCheckResult checkLinks(String content) {
        LinkCheckResult result = new LinkCheckResult();
        result.setLinks(new ArrayList<>());

        Pattern urlPattern = Pattern.compile("https?://[^\\s\\)]+");
        Matcher matcher = urlPattern.matcher(content);

        List<CompletableFuture<LinkCheckResult.LinkInfo>> futures = new ArrayList<>();

        while (matcher.find()) {
            String url = matcher.group();
            final int currentLine = findLineNumber(content, matcher.start());
            futures.add(checkSingleLink(url, currentLine));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<LinkCheckResult.LinkInfo> future : futures) {
            try {
                result.getLinks().add(future.get());
            } catch (Exception e) {
                log.error("获取链接检查结果失败", e);
            }
        }

        result.setTotal(result.getLinks().size());
        result.setValid((int) result.getLinks().stream()
                .filter(LinkCheckResult.LinkInfo::isValid)
                .count());
        result.setInvalid(result.getTotal() - result.getValid());

        return result;
    }

    private List<FormatRule> getEnabledRules(List<String> ruleKeys) {
        if (ruleKeys == null || ruleKeys.isEmpty()) {
            return ruleMapper.selectList(new LambdaQueryWrapper<FormatRule>()
                    .eq(FormatRule::getStatus, 1)
                    .eq(FormatRule::getIsDefault, 1)
                    .orderByAsc(FormatRule::getPriority));
        }

        return ruleMapper.selectList(new LambdaQueryWrapper<FormatRule>()
                .in(FormatRule::getRuleKey, ruleKeys)
                .eq(FormatRule::getStatus, 1)
                .orderByAsc(FormatRule::getPriority));
    }

    private FormatPreviewResult.Change applyRulePreview(String content, FormatRule rule) {
        try {
            if ("regex".equals(rule.getRuleType())) {
                return applyRegexPreview(content, rule);
            }
        } catch (Exception e) {
            log.error("预览规则失败: {}", rule.getRuleKey(), e);
        }
        return null;
    }

    private FormatPreviewResult.Change applyRegexPreview(String content, FormatRule rule) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            FormatPreviewResult.Change change = new FormatPreviewResult.Change();
            change.setRule(rule.getRuleKey());
            change.setDescription(rule.getRuleName());
            change.setDetails(new ArrayList<>());

            boolean skipInCode =
                    config.has("skipInCode") && config.get("skipInCode").asBoolean();
            String workingContent = content;
            Map<String, String> codeBlocks = new LinkedHashMap<>();

            if (skipInCode) {
                workingContent = extractCodeBlocks(content, codeBlocks);
            }

            if (config.has("patterns")) {
                for (JsonNode patternNode : config.get("patterns")) {
                    String pattern = patternNode.get("pattern").asText();
                    String replaceWith = patternNode.has("replaceWith")
                            ? patternNode.get("replaceWith").asText()
                            : patternNode.get("replacement").asText();

                    Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
                    Matcher m = p.matcher(workingContent);

                    while (m.find()) {
                        FormatPreviewResult.Detail detail = new FormatPreviewResult.Detail();
                        detail.setLine(findLineNumber(content, m.start()));
                        detail.setFrom(m.group());
                        detail.setTo(replaceWith);
                        change.getDetails().add(detail);
                    }
                }
            }

            change.setCount(change.getDetails().size());
            return change;
        } catch (Exception e) {
            log.error("应用正则预览失败: {}", rule.getRuleKey(), e);
            return null;
        }
    }

    private String applyRule(String content, FormatRule rule) {
        try {
            if ("regex".equals(rule.getRuleType())) {
                return applyRegex(content, rule);
            }
        } catch (Exception e) {
            log.error("应用规则失败: {}", rule.getRuleKey(), e);
        }
        return content;
    }

    private String applyRegex(String content, FormatRule rule) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            String result = content;

            boolean skipInCode =
                    config.has("skipInCode") && config.get("skipInCode").asBoolean();
            Map<String, String> codeBlocks = new LinkedHashMap<>();

            if (skipInCode) {
                result = extractCodeBlocks(result, codeBlocks);
            }

            if (config.has("patterns")) {
                for (JsonNode patternNode : config.get("patterns")) {
                    String pattern = patternNode.get("pattern").asText();
                    String replacement = patternNode.has("replacement")
                            ? patternNode.get("replacement").asText()
                            : patternNode.get("replaceWith").asText();
                    result = result.replaceAll(pattern, replacement);
                }
            }

            if (skipInCode) {
                result = restoreCodeBlocks(result, codeBlocks);
            }

            return result;
        } catch (Exception e) {
            log.error("应用正则规则失败: {}", rule.getRuleKey(), e);
            return content;
        }
    }

    private int findLineNumber(String content, int position) {
        return content.substring(0, position).split("\n").length;
    }

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```|`[^`]+`");

    private String extractCodeBlocks(String content, Map<String, String> codeBlocks) {
        AtomicInteger counter = new AtomicInteger(0);
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String placeholder = "___CODE_BLOCK_" + counter.getAndIncrement() + "___";
            codeBlocks.put(placeholder, matcher.group());
            matcher.appendReplacement(sb, placeholder);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private String restoreCodeBlocks(String content, Map<String, String> codeBlocks) {
        String result = content;
        for (Map.Entry<String, String> entry : codeBlocks.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 异步检查单个链接
     * 注意：此方法必须是 public，否则 @Async 注解无效（Spring AOP 无法拦截私有方法）
     */
    @Async
    public CompletableFuture<LinkCheckResult.LinkInfo> checkSingleLink(String url, int line) {
        LinkCheckResult.LinkInfo info = new LinkCheckResult.LinkInfo();
        info.setUrl(url);
        info.setLine(line);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            info.setStatus(response.statusCode());
            info.setValid(response.statusCode() >= 200 && response.statusCode() < 400);

            if (!info.isValid()) {
                info.setError("HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            info.setValid(false);
            info.setError(e.getMessage());
        }

        return CompletableFuture.completedFuture(info);
    }
}
