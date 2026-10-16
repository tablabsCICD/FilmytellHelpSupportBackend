package com.filmytell.support.repository;

import com.filmytell.support.entity.AiConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiConfigurationRepository extends JpaRepository<AiConfiguration, Long> {
    Optional<AiConfiguration> findByConfigKey(String configKey);
}
