package com.blog.common.utils;

import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public class HighlightUtil {

    private static final String HIGHLIGHT_PREFIX = "<em class=\"highlight\">";
    private static final String HIGHLIGHT_SUFFIX = "</em>";

    /**
     * 高亮处理关键词
     *
     * @param text      原文本
     * @param keyword   关键词
     * @param maxLength 返回最大长度(截取关键词上下文)
     * @return 高亮后的文本片段
     */
    public static String highlight(String text, String keyword, int maxLength) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return truncate(text, maxLength);
        }

        // 查找关键词位置(忽略大小写)
        int index = text.toLowerCase().indexOf(keyword.toLowerCase());

        // 如果找不到精确匹配，尝试查找部分匹配（中文分词情况）
        if (index == -1) {
            index = findPartialMatch(text, keyword);
        }

        if (index == -1) {
            // 实在找不到，从头截取但不高亮
            return truncate(text, maxLength);
        }

        // 截取关键词上下文
        String snippet = extractSnippet(text, index, maxLength);

        // 高亮关键词(忽略大小写)
        return snippet.replaceAll("(?i)(" + Pattern.quote(keyword) + ")", HIGHLIGHT_PREFIX + "$1" + HIGHLIGHT_SUFFIX);
    }

    /**
     * 仅高亮不截取
     */
    public static String highlightOnly(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return text;
        }
        return text.replaceAll("(?i)(" + Pattern.quote(keyword) + ")", HIGHLIGHT_PREFIX + "$1" + HIGHLIGHT_SUFFIX);
    }

    /**
     * 查找部分匹配位置（用于中文分词场景）
     * 当精确匹配找不到时，尝试找关键词中任意字符的匹配
     */
    private static int findPartialMatch(String text, String keyword) {
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        // 尝试找关键词的前缀（至少2个字符）
        for (int len = Math.min(keyword.length(), 4); len >= 2; len--) {
            String subKeyword = lowerKeyword.substring(0, len);
            int idx = lowerText.indexOf(subKeyword);
            if (idx != -1) {
                return idx;
            }
        }

        // 尝试找关键词中任意连续子串
        for (int i = 0; i < keyword.length() - 1; i++) {
            for (int len = 2; len <= keyword.length() - i; len++) {
                String subKeyword = lowerKeyword.substring(i, i + len);
                int idx = lowerText.indexOf(subKeyword);
                if (idx != -1) {
                    return idx;
                }
            }
        }

        return -1;
    }

    private static String extractSnippet(String text, int index, int maxLength) {
        // 计算上下文范围，确保关键词在片段中间位置
        int contextBefore = maxLength / 3;
        int contextAfter = maxLength - contextBefore;

        int start = Math.max(0, index - contextBefore);
        int end = Math.min(text.length(), index + contextAfter);

        String snippet = text.substring(start, end);
        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < text.length()) {
            snippet = snippet + "...";
        }
        return snippet;
    }

    private static String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
