package com.filmytell.support.dto.knowledge;

import com.filmytell.support.enums.KnowledgeStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticleResponse {

    private Long id;
    private String title;
    private String category;
    private String content;
    private String keywords;
    private KnowledgeStatus status;
    private int version;
    private Long createdById;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
}
