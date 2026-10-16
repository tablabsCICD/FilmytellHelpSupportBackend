package com.filmytell.support.service;

import com.filmytell.support.dto.knowledge.KnowledgeArticleRequest;
import com.filmytell.support.dto.knowledge.KnowledgeArticleResponse;
import com.filmytell.support.entity.KnowledgeArticle;
import com.filmytell.support.entity.User;
import com.filmytell.support.enums.KnowledgeStatus;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.repository.KnowledgeArticleRepository;
import com.filmytell.support.repository.UserRepository;
import com.filmytell.support.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public KnowledgeArticleResponse createArticle(KnowledgeArticleRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        KnowledgeArticle article = KnowledgeArticle.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .keywords(request.getKeywords())
                .status(request.getStatus() != null ? request.getStatus() : KnowledgeStatus.PUBLISHED)
                .version(1)
                .createdByUser(currentUser)
                .updatedByUser(currentUser)
                .build();

        return toResponse(articleRepository.save(article));
    }

    @Transactional
    public KnowledgeArticleResponse updateArticle(Long id, KnowledgeArticleRequest request) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Article", "id", id));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setContent(request.getContent());
        article.setKeywords(request.getKeywords());
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }
        article.setVersion(article.getVersion() + 1);
        article.setUpdatedByUser(currentUser);

        return toResponse(articleRepository.save(article));
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleResponse> getAllArticles(KnowledgeStatus status) {
        List<KnowledgeArticle> articles = status != null
                ? articleRepository.findByStatus(status)
                : articleRepository.findAll();
        return articles.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleResponse> searchArticles(String keyword) {
        return articleRepository.searchPublishedArticles(keyword).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeArticleResponse getArticleById(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Article", "id", id));
        return toResponse(article);
    }

    @Transactional
    public void deleteArticle(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Article", "id", id));
        article.setStatus(KnowledgeStatus.ARCHIVED);
        articleRepository.save(article);
    }

    private KnowledgeArticleResponse toResponse(KnowledgeArticle a) {
        return KnowledgeArticleResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .category(a.getCategory())
                .content(a.getContent())
                .keywords(a.getKeywords())
                .status(a.getStatus())
                .version(a.getVersion())
                .createdById(a.getCreatedByUser() != null ? a.getCreatedByUser().getId() : null)
                .createdByName(a.getCreatedByUser() != null ? a.getCreatedByUser().getFullName() : "SYSTEM")
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
