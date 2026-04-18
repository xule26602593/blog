package com.blog.repository.mapper;

/**
 * ArticleMapper 的 SQL Provider，用于动态 SQL
 */
public class ArticleMapperProvider {

    /**
     * 查找同标签的上一篇文章
     * tagIdsStr: 逗号分隔的标签ID字符串，如 "1,2,3"
     */
    public String selectPrevByTags(Long currentId, String tagIdsStr) {
        if (tagIdsStr == null || tagIdsStr.isEmpty()) {
            // 返回一个永远为空的结果
            return "SELECT a.id, a.title, c.name AS categoryName " +
                   "FROM article a LEFT JOIN category c ON 1=0 WHERE 1=0";
        }
        // Validate format: only digits and commas to prevent SQL injection
        if (!tagIdsStr.matches("^[0-9]+(,[0-9]+)*$")) {
            throw new IllegalArgumentException("Invalid tagIdsStr format: must be comma-separated numbers");
        }
        return """
            SELECT a.id, a.title, c.name AS categoryName
            FROM article a
            LEFT JOIN category c ON a.category_id = c.id
            INNER JOIN article_tag at ON a.id = at.article_id
            WHERE a.deleted = 0 AND a.status = 1
              AND at.tag_id IN (${tagIdsStr})
              AND a.publish_time < (SELECT publish_time FROM article WHERE id = #{currentId})
            ORDER BY a.publish_time DESC
            LIMIT 1
            """;
    }

    /**
     * 查找同标签的下一篇文章
     * tagIdsStr: 逗号分隔的标签ID字符串，如 "1,2,3"
     */
    public String selectNextByTags(Long currentId, String tagIdsStr) {
        if (tagIdsStr == null || tagIdsStr.isEmpty()) {
            return "SELECT a.id, a.title, c.name AS categoryName " +
                   "FROM article a LEFT JOIN category c ON 1=0 WHERE 1=0";
        }
        // Validate format: only digits and commas to prevent SQL injection
        if (!tagIdsStr.matches("^[0-9]+(,[0-9]+)*$")) {
            throw new IllegalArgumentException("Invalid tagIdsStr format: must be comma-separated numbers");
        }
        return """
            SELECT a.id, a.title, c.name AS categoryName
            FROM article a
            LEFT JOIN category c ON a.category_id = c.id
            INNER JOIN article_tag at ON a.id = at.article_id
            WHERE a.deleted = 0 AND a.status = 1
              AND at.tag_id IN (${tagIdsStr})
              AND a.publish_time > (SELECT publish_time FROM article WHERE id = #{currentId})
            ORDER BY a.publish_time ASC
            LIMIT 1
            """;
    }
}
