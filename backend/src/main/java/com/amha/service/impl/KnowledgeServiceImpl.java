package com.amha.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.amha.common.BusinessException;
import com.amha.common.PageResult;
import com.amha.dto.*;
import com.amha.entity.KnowledgeArticle;
import com.amha.entity.KnowledgeCategory;
import com.amha.mapper.KnowledgeArticleMapper;
import com.amha.mapper.KnowledgeCategoryMapper;
import com.amha.service.KnowledgeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeArticleMapper articleMapper;

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<KnowledgeCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>()
                        .eq(KnowledgeCategory::getStatus, 1)
                        .orderByAsc(KnowledgeCategory::getSortOrder));

        return categories.stream().map(cat -> {
            Long articleCount = articleMapper.selectCount(
                    new LambdaQueryWrapper<KnowledgeArticle>()
                            .eq(KnowledgeArticle::getCategoryId, cat.getId())
                            .eq(KnowledgeArticle::getStatus, 1));
            return CategoryVO.builder()
                    .id(cat.getId())
                    .categoryName(cat.getCategoryName())
                    .description(cat.getDescription())
                    .sortOrder(cat.getSortOrder())
                    .status(cat.getStatus())
                    .statusText(cat.getStatus() == 1 ? "启用" : "禁用")
                    .articleCount(articleCount.intValue())
                    .createdAt(cat.getCreatedAt())
                    .updatedAt(cat.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<ArticleVO> getArticlePage(ArticlePageQuery query) {
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getTitle())) {
            wrapper.like(KnowledgeArticle::getTitle, query.getTitle());
        }
        if (StrUtil.isNotBlank(query.getCategoryId())) {
            wrapper.eq(KnowledgeArticle::getCategoryId, Long.parseLong(query.getCategoryId()));
        }
        if (StrUtil.isNotBlank(query.getStatus())) {
            wrapper.eq(KnowledgeArticle::getStatus, Integer.parseInt(query.getStatus()));
        }
        if (StrUtil.isNotBlank(query.getAuthorName())) {
            wrapper.like(KnowledgeArticle::getAuthorName, query.getAuthorName());
        }
        wrapper.orderByDesc(KnowledgeArticle::getCreatedAt);

        Page<KnowledgeArticle> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        IPage<KnowledgeArticle> result = articleMapper.selectPage(page, wrapper);

        List<ArticleVO> records = result.getRecords().stream().map(article -> {
            KnowledgeCategory category = categoryMapper.selectById(article.getCategoryId());
            return ArticleVO.builder()
                    .id(article.getId())
                    .categoryId(article.getCategoryId())
                    .categoryName(category != null ? category.getCategoryName() : "")
                    .title(article.getTitle())
                    .summary(article.getSummary())
                    .coverImage(article.getCoverImage())
                    .tags(article.getTags())
                    .authorName(article.getAuthorName())
                    .readCount(article.getReadCount())
                    .status(article.getStatus())
                    .statusText(getStatusText(article.getStatus()))
                    .isFavorite(article.getIsFavorite() == 1)
                    .favoriteCount(article.getFavoriteCount())
                    .publishedAt(article.getPublishedAt())
                    .createdAt(article.getCreatedAt())
                    .updatedAt(article.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());

        return PageResult.of(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public void createArticle(ArticleSaveRequest request, Long userId, String username) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setCategoryId(request.getCategoryId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setTags(request.getTags());
        article.setAuthorId(userId);
        article.setAuthorName(username);
        article.setStatus(0);
        articleMapper.insert(article);
    }

    @Override
    public ArticleDetailVO getArticleById(Long id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        KnowledgeCategory category = categoryMapper.selectById(article.getCategoryId());

        List<String> tagArray = StrUtil.isNotBlank(article.getTags())
                ? Arrays.asList(article.getTags().split(","))
                : List.of();

        return ArticleDetailVO.builder()
                .id(article.getId())
                .categoryId(article.getCategoryId())
                .categoryName(category != null ? category.getCategoryName() : "")
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImage(article.getCoverImage())
                .tags(article.getTags())
                .tagArray(tagArray)
                .authorId(article.getAuthorId())
                .authorName(article.getAuthorName())
                .readCount(article.getReadCount())
                .status(article.getStatus())
                .statusText(getStatusText(article.getStatus()))
                .isFavorited(false)
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    @Override
    public void updateArticle(Long id, ArticleSaveRequest request) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        article.setCategoryId(request.getCategoryId());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setTags(request.getTags());
        if (request.getCategoryId() != null) {
            article.setCategoryId(request.getCategoryId());
        }
        articleMapper.updateById(article);
    }

    @Override
    public void changeArticleStatus(Long id, Integer status) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        article.setStatus(status);
        if (status == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(article);
    }

    @Override
    public void deleteArticle(Long id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        articleMapper.deleteById(id);
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "已下架";
            default -> "未知";
        };
    }
}
