package com.blog.domain.enums;

import lombok.Getter;

/**
 * AI分析状态枚举
 */
@Getter
public enum AnalysisStatus {
    PENDING(0, "pending", "待分析"),
    ANALYZING(1, "analyzing", "分析中"),
    COMPLETED(2, "completed", "已完成"),
    FAILED(3, "failed", "失败");

    private final Integer code;
    private final String name;
    private final String desc;

    AnalysisStatus(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static AnalysisStatus fromCode(Integer code) {
        if (code == null) return null;
        for (AnalysisStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String nameFromCode(Integer code) {
        AnalysisStatus status = fromCode(code);
        return status != null ? status.getName() : null;
    }
}
