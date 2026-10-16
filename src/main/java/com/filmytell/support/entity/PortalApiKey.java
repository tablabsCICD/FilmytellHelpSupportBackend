package com.filmytell.support.entity;

import com.filmytell.support.enums.SourcePortal;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "portal_api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalApiKey extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "portal_name", nullable = false, length = 50)
    private SourcePortal portalName;

    @Column(name = "api_key_hash", nullable = false, unique = true, length = 255)
    private String apiKeyHash;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "description")
    private String description;
}
