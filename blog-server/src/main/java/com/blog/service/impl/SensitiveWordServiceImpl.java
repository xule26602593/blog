package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.SensitiveWordFilter;
import com.blog.domain.dto.SensitiveWordDTO;
import com.blog.domain.entity.SensitiveWord;
import com.blog.domain.vo.SensitiveWordVO;
import com.blog.repository.mapper.SensitiveWordMapper;
import com.blog.service.SensitiveWordService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    public Page<SensitiveWordVO> pageList(String word, String category, int pageNum, int pageSize) {
        Page<SensitiveWord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();

        if (word != null && !word.isEmpty()) {
            wrapper.like(SensitiveWord::getWord, word);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(SensitiveWord::getCategory, category);
        }
        wrapper.orderByDesc(SensitiveWord::getCreateTime);

        Page<SensitiveWord> result = sensitiveWordMapper.selectPage(page, wrapper);

        Page<SensitiveWordVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(entity -> BeanCopyUtils.copy(entity, SensitiveWordVO.class))
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public void add(SensitiveWordDTO dto) {
        // 检查是否已存在
        Long count = sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getWord, dto.getWord()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "敏感词已存在");
        }

        SensitiveWord entity = new SensitiveWord();
        entity.setWord(dto.getWord());
        entity.setCategory(dto.getCategory());
        entity.setReplaceWord(dto.getReplaceWord() != null ? dto.getReplaceWord() : "*");
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        sensitiveWordMapper.insert(entity);
        refreshCache();
    }

    @Override
    @Transactional
    public void batchAdd(List<SensitiveWordDTO> list) {
        for (SensitiveWordDTO dto : list) {
            try {
                add(dto);
            } catch (BusinessException e) {
                // 忽略重复的敏感词
            }
        }
    }

    @Override
    @Transactional
    public void update(Long id, SensitiveWordDTO dto) {
        SensitiveWord entity = sensitiveWordMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "敏感词不存在");
        }

        entity.setWord(dto.getWord());
        entity.setCategory(dto.getCategory());
        entity.setReplaceWord(dto.getReplaceWord());
        entity.setStatus(dto.getStatus());

        sensitiveWordMapper.updateById(entity);
        refreshCache();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sensitiveWordMapper.deleteById(id);
        refreshCache();
    }

    @Override
    public String filter(String text) {
        return sensitiveWordFilter.filter(text);
    }

    @Override
    public boolean contains(String text) {
        return sensitiveWordFilter.contains(text);
    }

    @Override
    public void refreshCache() {
        List<String> words = sensitiveWordMapper.selectAllEnabledWords();
        sensitiveWordFilter.init(words);
    }
}
