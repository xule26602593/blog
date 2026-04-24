package com.blog.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * DFA 算法敏感词过滤器
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    private Map<Character, Object> sensitiveWordMap = new HashMap<>();

    private static final String REPLACE_MASK = "***";

    /**
     * 初始化敏感词库
     */
    @SuppressWarnings("unchecked")
    public synchronized void init(List<String> words) {
        Map<Character, Object> newMap = new HashMap<>();
        for (String word : words) {
            if (word == null || word.isEmpty()) continue;

            Map<Character, Object> currentMap = newMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Object obj = currentMap.get(c);
                if (obj == null) {
                    Map<Character, Object> childMap = new HashMap<>();
                    currentMap.put(c, childMap);
                    currentMap = childMap;
                } else {
                    currentMap = (Map<Character, Object>) obj;
                }
            }
            // 标记词尾
            currentMap.put((char) 0, true);
        }
        this.sensitiveWordMap = newMap;
        log.info("敏感词库初始化完成，共 {} 个敏感词", words.size());
    }

    /**
     * 检测文本中是否包含敏感词
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) return false;

        for (int i = 0; i < text.length(); i++) {
            if (checkWord(text, i) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤敏感词，替换为 ***
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder(text);
        int wordLength;
        for (int i = 0; i < result.length(); i++) {
            wordLength = checkWord(result.toString(), i);
            if (wordLength > 0) {
                result.replace(i, i + wordLength, REPLACE_MASK);
                i += REPLACE_MASK.length() - 1;
            }
        }
        return result.toString();
    }

    /**
     * 从指定位置检测敏感词长度
     */
    @SuppressWarnings("unchecked")
    private int checkWord(String text, int beginIndex) {
        Map<Character, Object> currentMap = sensitiveWordMap;
        int wordLength = 0;
        boolean foundEnd = false;

        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            currentMap = (Map<Character, Object>) currentMap.get(c);
            if (currentMap == null) {
                break;
            }
            wordLength++;
            if (currentMap.containsKey((char) 0)) {
                foundEnd = true;
            }
        }

        return foundEnd ? wordLength : 0;
    }
}
