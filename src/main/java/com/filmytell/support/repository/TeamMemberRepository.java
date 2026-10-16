package com.filmytell.support.repository;

import com.filmytell.support.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamIdAndActiveTrue(Long teamId);
    List<TeamMember> findByUserIdAndActiveTrue(Long userId);
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
    boolean existsByTeamIdAndUserIdAndActiveTrue(Long teamId, Long userId);
}
