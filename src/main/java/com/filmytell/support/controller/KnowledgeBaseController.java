package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.knowledge.KnowledgeArticleRequest;
import com.filmytell.support.dto.knowledge.KnowledgeArticleResponse;
import com.filmytell.support.enums.KnowledgeStatus;
import com.filmytell.support.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge-base")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base & AI Grounding", description = "Endpoints for managing verified support articles used for grounding AI and assisting agents")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @GetMapping
    @Operation(summary = "Get list of knowledge base articles")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleResponse>>> getArticles(
            @RequestParam(required = false) KnowledgeStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeBaseService.getAllArticles(status)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search knowledge articles by keyword/category")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleResponse>>> searchArticles(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeBaseService.searchArticles(query)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get knowledge article by ID")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(knowledgeBaseService.getArticleById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('AI_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Create a new knowledge base article")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> createArticle(
            @Valid @RequestBody KnowledgeArticleRequest request
    ) {
        KnowledgeArticleResponse response = knowledgeBaseService.createArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Article created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AI_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Update knowledge base article and increment version")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeArticleRequest request
    ) {
        KnowledgeArticleResponse response = knowledgeBaseService.updateArticle(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Article updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AI_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Archive knowledge base article")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        knowledgeBaseService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.ok("Article archived successfully", null));
    }
}
