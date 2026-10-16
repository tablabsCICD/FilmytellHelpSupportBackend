package com.filmytell.support.mapper;

import com.filmytell.support.dto.auth.UserProfileDto;
import com.filmytell.support.dto.team.TeamMemberDto;
import com.filmytell.support.dto.team.TeamResponse;
import com.filmytell.support.entity.Permission;
import com.filmytell.support.entity.Role;
import com.filmytell.support.entity.Team;
import com.filmytell.support.entity.TeamMember;
import com.filmytell.support.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserProfileDto toProfileDto(User user) {
        if (user == null) return null;

        Set<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<String> permissions = user.getRoles() != null
                ? user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName().name())
                .collect(Collectors.toSet())
                : Collections.emptySet();

        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .active(user.isActive())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public TeamMemberDto toTeamMemberDto(TeamMember member) {
        if (member == null) return null;

        return TeamMemberDto.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .roleInTeam(member.getRoleInTeam())
                .active(member.isActive())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public TeamResponse toTeamResponse(Team team) {
        if (team == null) return null;

        List<TeamMemberDto> memberDtos = team.getMembers() != null
                ? team.getMembers().stream().map(this::toTeamMemberDto).toList()
                : Collections.emptyList();

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .code(team.getCode())
                .description(team.getDescription())
                .active(team.isActive())
                .memberCount(memberDtos.size())
                .members(memberDtos)
                .createdAt(team.getCreatedAt())
                .build();
    }
}
