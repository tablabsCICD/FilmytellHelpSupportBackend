package com.filmytell.support.controller;

import com.filmytell.support.dto.auth.AuthResponse;
import com.filmytell.support.dto.auth.LoginRequest;
import com.filmytell.support.dto.auth.RefreshTokenRequest;
import com.filmytell.support.dto.auth.RegisterUserRequest;
import com.filmytell.support.dto.auth.UserProfileDto;
import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, JWT tokens, and agent onboarding")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with username/email and password", description = "Authenticates user and returns JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token", description = "Generates a new access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('USER_MANAGE', 'ROLE_ADMIN')")
    @Operation(summary = "Register a new team agent (Admin/Manager only)", description = "Creates a new internal support user")
    public ResponseEntity<ApiResponse<UserProfileDto>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        UserProfileDto userProfile = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", userProfile));
    }
}
