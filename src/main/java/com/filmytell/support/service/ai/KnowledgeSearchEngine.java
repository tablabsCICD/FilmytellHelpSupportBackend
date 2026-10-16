package com.filmytell.support.service.ai;

import com.filmytell.support.entity.KnowledgeArticle;
import com.filmytell.support.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchEngine {

    private final KnowledgeArticleRepository knowledgeArticleRepository;

    @Transactional(readOnly = true)
    public Optional<KnowledgeArticle> findBestMatchingArticle(String subject, String description, String category) {
        String query = subject + " " + (description != null ? description : "");
        String[] keywords = query.toLowerCase().split("\\W+");

        for (String word : keywords) {
            if (word.length() > 3) {
                List<KnowledgeArticle> matches = knowledgeArticleRepository.searchPublishedArticles(word);
                if (!matches.isEmpty()) {
                    return Optional.of(matches.get(0));
                }
            }
        }

        // Fallback: search by category
        if (category != null && !category.isBlank()) {
            List<KnowledgeArticle> catMatches = knowledgeArticleRepository.searchPublishedArticles(category);
            if (!catMatches.isEmpty()) {
                return Optional.of(catMatches.get(0));
            }
        }

        return Optional.empty();
    }
}
