package com.filmytell.support.dto.knowledge;

import com.filmytell.support.enums.KnowledgeStatus;
import javax.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticleRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Content is required")
    private String content;

    private String keywords;

    @Builder.Default
    private KnowledgeStatus status = KnowledgeStatus.PUBLISHED;
}
