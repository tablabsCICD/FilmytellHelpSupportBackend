package com.filmytell.support.service;

import com.filmytell.support.dto.auth.AuthResponse;
import com.filmytell.support.dto.auth.LoginRequest;
import com.filmytell.support.dto.auth.RefreshTokenRequest;
import com.filmytell.support.dto.auth.RegisterUserRequest;
import com.filmytell.support.dto.auth.UserProfileDto;
import com.filmytell.support.entity.RefreshToken;
import com.filmytell.support.entity.Role;
import com.filmytell.support.entity.Team;
import com.filmytell.support.entity.TeamMember;
import com.filmytell.support.entity.User;
import com.filmytell.support.enums.RoleName;
import com.filmytell.support.exception.DuplicateResourceException;
import com.filmytell.support.exception.ResourceNotFoundException;
import com.filmytell.support.exception.UnauthorizedOperationException;
import com.filmytell.support.mapper.UserMapper;
import com.filmytell.support.repository.RefreshTokenRepository;
import com.filmytell.support.repository.RoleRepository;
import com.filmytell.support.repository.TeamMemberRepository;
import com.filmytell.support.repository.TeamRepository;
import com.filmytell.support.repository.UserRepository;
import com.filmytell.support.security.JwtTokenProvider;
import com.filmytell.support.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userPrincipal.getId()));

        RefreshToken refreshToken = createRefreshToken(user);

        Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedOperationException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedOperationException("Refresh token has expired. Please login again.");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        String newJwt = tokenProvider.generateToken(auth);

        Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(newJwt)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public UserProfileDto registerUser(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use: " + request.getEmail());
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleStr : request.getRoles()) {
                try {
                    RoleName roleName = RoleName.valueOf(roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr);
                    roleRepository.findByName(roleName).ifPresent(roles::add);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (roles.isEmpty()) {
            roleRepository.findByName(RoleName.ROLE_SUPPORT_MEMBER).ifPresent(roles::add);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        if (request.getTeamId() != null) {
            teamRepository.findById(request.getTeamId()).ifPresent(team -> {
                TeamMember member = TeamMember.builder()
                        .team(team)
                        .user(savedUser)
                        .roleInTeam("MEMBER")
                        .active(true)
                        .build();
                teamMemberRepository.save(member);
            });
        }

        return userMapper.toProfileDto(savedUser);
    }

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
