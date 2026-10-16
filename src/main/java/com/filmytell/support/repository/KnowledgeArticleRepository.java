package com.filmytell.support.repository;

import com.filmytell.support.entity.KnowledgeArticle;
import com.filmytell.support.enums.KnowledgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByStatus(KnowledgeStatus status);

    List<KnowledgeArticle> findByCategoryAndStatus(String category, KnowledgeStatus status);

    @Query("""
        SELECT a FROM KnowledgeArticle a
        WHERE a.status = 'PUBLISHED'
          AND (
            LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    List<KnowledgeArticle> searchPublishedArticles(String keyword);
}
