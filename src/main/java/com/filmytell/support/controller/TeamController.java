package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.team.AddTeamMemberRequest;
import com.filmytell.support.dto.team.TeamRequest;
import com.filmytell.support.dto.team.TeamResponse;
import com.filmytell.support.service.TeamService;
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
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams Management", description = "Endpoints for Support and Technical Support teams management")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get list of all active support teams and members")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getAllTeams()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getTeamById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TEAM_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Create a new support team")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Team created successfully", response));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyAuthority('TEAM_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Add member to support team")
    public ResponseEntity<ApiResponse<TeamResponse>> addMemberToTeam(
            @PathVariable Long id,
            @Valid @RequestBody AddTeamMemberRequest request
    ) {
        TeamResponse response = teamService.addMemberToTeam(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Member added to team successfully", response));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyAuthority('TEAM_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Deactivate member in support team")
    public ResponseEntity<ApiResponse<Void>> removeMemberFromTeam(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        teamService.removeMemberFromTeam(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Member deactivated from team successfully", null));
    }
}
