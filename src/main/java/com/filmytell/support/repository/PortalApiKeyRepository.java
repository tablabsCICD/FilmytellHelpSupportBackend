package com.filmytell.support.repository;

import com.filmytell.support.entity.PortalApiKey;
import com.filmytell.support.enums.SourcePortal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortalApiKeyRepository extends JpaRepository<PortalApiKey, Long> {
    Optional<PortalApiKey> findByApiKeyHashAndActiveTrue(String apiKeyHash);
    Optional<PortalApiKey> findByPortalNameAndApiKeyHashAndActiveTrue(SourcePortal portalName, String apiKeyHash);
}
