package com.filmytell.support.service;

import com.filmytell.support.dto.team.AddTeamMemberRequest;
import com.filmytell.support.dto.team.TeamRequest;
import com.filmytell.support.dto.team.TeamResponse;
import com.filmytell.support.entity.Team;
import com.filmytell.support.entity.TeamMember;
import com.filmytell.support.entity.User;
import com.filmytell.support.exception.DuplicateResourceException;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.mapper.UserMapper;
import com.filmytell.support.repository.TeamMemberRepository;
import com.filmytell.support.repository.TeamRepository;
import com.filmytell.support.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream().map(userMapper::toTeamResponse).toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        return userMapper.toTeamResponse(team);
    }

    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        if (teamRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("Team code already exists: " + request.getCode());
        }

        Team team = Team.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .active(true)
                .build();

        return userMapper.toTeamResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamResponse addMemberToTeam(Long teamId, AddTeamMemberRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        if (teamMemberRepository.existsByTeamIdAndUserIdAndActiveTrue(teamId, user.getId())) {
            throw new DuplicateResourceException("User is already an active member of this team");
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .roleInTeam(request.getRoleInTeam() != null ? request.getRoleInTeam() : "MEMBER")
                .active(true)
                .build();

        teamMemberRepository.save(member);
        team.getMembers().add(member);

        return userMapper.toTeamResponse(team);
    }

    @Transactional
    public void removeMemberFromTeam(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in team"));
        member.setActive(false);
        teamMemberRepository.save(member);
    }
}
