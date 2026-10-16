package com.filmytell.support.controller;

import com.filmytell.support.dto.auth.UserProfileDto;
import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.security.SecurityUtils;
import com.filmytell.support.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for viewing agents, profiles, and active support staff")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Get list of all users")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @GetMapping("/agents")
    @PreAuthorize("hasAnyAuthority('TICKET_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get list of active support staff and engineers for assignment")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getActiveAgents() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getActiveAgents()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(currentUserId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('USER_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Activate or deactivate user account")
    public ResponseEntity<ApiResponse<UserProfileDto>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userService.toggleUserStatus(id, active)));
    }
}
