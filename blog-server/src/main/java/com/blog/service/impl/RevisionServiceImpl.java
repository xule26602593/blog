package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.ArticleRevision;
import com.blog.domain.entity.User;
import com.blog.domain.vo.RevisionVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.ArticleRevisionMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.RevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevisionServiceImpl implements RevisionService {

    private final ArticleRevisionMapper revisionMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    private static final int MAX_VERSIONS = 20;

    @Override
    @Transactional
    public void createSnapshot(Long articleId, Long editorId, String changeNote) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
        }

        Integer maxVersion = revisionMapper.selectMaxVersion(articleId);
        int newVersion = (maxVersion != null ? maxVersion : 0) + 1;

        ArticleRevision revision = new ArticleRevision();
        revision.setArticleId(articleId);
        revision.setVersion(newVersion);
        revision.setTitle(article.getTitle());
        revision.setContent(article.getContent());
        revision.setSummary(article.getSummary());
        revision.setEditorId(editorId);
        revision.setChangeNote(changeNote);

        revisionMapper.insert(revision);

        // 清理旧版本，保留最近 MAX_VERSIONS 个
        cleanOldRevisions(articleId);
    }

    @Override
    public Page<RevisionVO> getRevisions(Long articleId, int pageNum, int pageSize) {
        Page<ArticleRevision> page = new Page<>(pageNum, pageSize);
        Page<ArticleRevision> result = revisionMapper.selectPage(page,
                new LambdaQueryWrapper<ArticleRevision>()
                        .eq(ArticleRevision::getArticleId, articleId)
                        .orderByDesc(ArticleRevision::getVersion));

        Page<RevisionVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public RevisionVO getRevision(Long articleId, Integer version) {
        ArticleRevision revision = revisionMapper.selectOne(
                new LambdaQueryWrapper<ArticleRevision>()
                        .eq(ArticleRevision::getArticleId, articleId)
                        .eq(ArticleRevision::getVersion, version));

        if (revision == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "版本不存在");
        }

        return convertToVO(revision);
    }

    @Override
    @Transactional
    public void restore(Long articleId, Integer version, Long editorId) {
        ArticleRevision revision = revisionMapper.selectOne(
                new LambdaQueryWrapper<ArticleRevision>()
                        .eq(ArticleRevision::getArticleId, articleId)
                        .eq(ArticleRevision::getVersion, version));

        if (revision == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "版本不存在");
        }

        Article article = new Article();
        article.setId(articleId);
        article.setTitle(revision.getTitle());
        article.setContent(revision.getContent());
        article.setSummary(revision.getSummary());

        articleMapper.updateById(article);

        // 创建新快照记录回退操作
        createSnapshot(articleId, editorId, "回退到版本 " + version);
    }

    private void cleanOldRevisions(Long articleId) {
        Long count = revisionMapper.selectCount(
                new LambdaQueryWrapper<ArticleRevision>()
                        .eq(ArticleRevision::getArticleId, articleId));

        if (count > MAX_VERSIONS) {
            // 删除最旧的版本
            revisionMapper.delete(
                    new LambdaQueryWrapper<ArticleRevision>()
                            .eq(ArticleRevision::getArticleId, articleId)
                            .orderByAsc(ArticleRevision::getVersion)
                            .last("LIMIT " + (count - MAX_VERSIONS)));
        }
    }

    private RevisionVO convertToVO(ArticleRevision revision) {
        RevisionVO vo = BeanCopyUtils.copy(revision, RevisionVO.class);

        if (revision.getEditorId() != null) {
            User user = userMapper.selectById(revision.getEditorId());
            if (user != null) {
                vo.setEditorNickname(user.getNickname());
            }
        }

        return vo;
    }
}
