package com.filmytell.support.config;

import com.filmytell.support.entity.*;
import com.filmytell.support.enums.*;
import com.filmytell.support.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PortalApiKeyRepository portalApiKeyRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Database already initialized. Skipping DataInitializer.");
            return;
        }

        log.info("Seeding initial database records for local development...");

        // 1. Permissions
        Set<Permission> allPermissions = new HashSet<>();
        for (PermissionName permName : PermissionName.values()) {
            Permission p = Permission.builder()
                    .name(permName)
                    .description("Permission " + permName.name())
                    .build();
            allPermissions.add(permissionRepository.save(p));
        }

        // 2. Roles
        Role adminRole = Role.builder()
                .name(RoleName.ROLE_ADMIN)
                .description("System Administrator")
                .permissions(allPermissions)
                .build();
        roleRepository.save(adminRole);

        Role managerRole = Role.builder()
                .name(RoleName.ROLE_SUPPORT_MANAGER)
                .description("Support Team Manager")
                .permissions(allPermissions)
                .build();
        roleRepository.save(managerRole);

        Role supportRole = Role.builder()
                .name(RoleName.ROLE_SUPPORT_MEMBER)
                .description("Support Agent")
                .permissions(allPermissions)
                .build();
        roleRepository.save(supportRole);

        Role techSupportRole = Role.builder()
                .name(RoleName.ROLE_TECHNICAL_SUPPORT_MEMBER)
                .description("Technical Support Engineer")
                .permissions(allPermissions)
                .build();
        roleRepository.save(techSupportRole);

        // 3. Teams
        Team supportTeam = Team.builder()
                .name("Support Team")
                .code("SUPPORT_GENERAL")
                .description("General Customer Support Team")
                .active(true)
                .build();
        supportTeam = teamRepository.save(supportTeam);

        Team techTeam = Team.builder()
                .name("Technical Support Team")
                .code("TECH_SUPPORT")
                .description("Engineering & Video Infrastructure Support Team")
                .active(true)
                .build();
        techTeam = teamRepository.save(techTeam);

        // 4. Users
        String encodedPass = passwordEncoder.encode("Password@123");

        User adminUser = User.builder()
                .username("admin")
                .email("admin@filmytell.com")
                .passwordHash(encodedPass)
                .firstName("Super")
                .lastName("Admin")
                .active(true)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(adminUser);

        User managerUser = User.builder()
                .username("manager_rahul")
                .email("rahul.manager@filmytell.com")
                .passwordHash(encodedPass)
                .firstName("Rahul")
                .lastName("Sharma")
                .active(true)
                .roles(Set.of(managerRole))
                .build();
        userRepository.save(managerUser);

        User aliceUser = User.builder()
                .username("support_alice")
                .email("alice.support@filmytell.com")
                .passwordHash(encodedPass)
                .firstName("Alice")
                .lastName("Verma")
                .active(true)
                .roles(Set.of(supportRole))
                .build();
        userRepository.save(aliceUser);

        User bobUser = User.builder()
                .username("support_bob")
                .email("bob.support@filmytell.com")
                .passwordHash(encodedPass)
                .firstName("Bob")
                .lastName("Singh")
                .active(true)
                .roles(Set.of(supportRole))
                .build();
        userRepository.save(bobUser);

        User charlieUser = User.builder()
                .username("tech_charlie")
                .email("charlie.tech@filmytell.com")
                .passwordHash(encodedPass)
                .firstName("Charlie")
                .lastName("Patel")
                .active(true)
                .roles(Set.of(techSupportRole))
                .build();
        userRepository.save(charlieUser);

        User davidUser = User.builder()
                .username("tech_david")
                .email("david.tech@filmytell.com")
                .passwordHash(encodedPass)
                .firstName("David")
                .lastName("Khan")
                .active(true)
                .roles(Set.of(techSupportRole))
                .build();
        userRepository.save(davidUser);

        // 5. Portal API Keys
        portalApiKeyRepository.save(PortalApiKey.builder()
                .portalName(SourcePortal.FILMYTELL)
                .apiKeyHash(passwordEncoder.encode("filmytell-portal-api-key-2026"))
                .description("Filmytell App & Web")
                .active(true)
                .build());

        portalApiKeyRepository.save(PortalApiKey.builder()
                .portalName(SourcePortal.FILMYTELL_PARTNER)
                .apiKeyHash(passwordEncoder.encode("filmytell-partner-portal-api-key-2026"))
                .description("Filmytell Partner Portal")
                .active(true)
                .build());

        portalApiKeyRepository.save(PortalApiKey.builder()
                .portalName(SourcePortal.PRODUCTION_HOUSE)
                .apiKeyHash(passwordEncoder.encode("production-house-portal-api-key-2026"))
                .description("Production House Portal")
                .active(true)
                .build());

        // 6. SLA Policies
        slaPolicyRepository.save(SlaPolicy.builder()
                .name("Urgent Severity SLA")
                .priority(TicketPriority.URGENT)
                .firstResponseTimeMinutes(30)
                .resolutionTimeMinutes(120)
                .active(true)
                .build());

        slaPolicyRepository.save(SlaPolicy.builder()
                .name("High Severity SLA")
                .priority(TicketPriority.HIGH)
                .firstResponseTimeMinutes(60)
                .resolutionTimeMinutes(240)
                .active(true)
                .build());

        slaPolicyRepository.save(SlaPolicy.builder()
                .name("Medium Severity SLA")
                .priority(TicketPriority.MEDIUM)
                .firstResponseTimeMinutes(240)
                .resolutionTimeMinutes(720)
                .active(true)
                .build());

        slaPolicyRepository.save(SlaPolicy.builder()
                .name("Low Severity SLA")
                .priority(TicketPriority.LOW)
                .firstResponseTimeMinutes(480)
                .resolutionTimeMinutes(1440)
                .active(true)
                .build());

        // 7. Knowledge Base
        knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .title("How to Reset Password on Filmytell")
                .category("ACCOUNT_LOGIN")
                .content("To reset your password, click 'Forgot Password' on the login screen, enter your registered email address, and follow the OTP verification link sent to your inbox.")
                .keywords("password,reset,login,forgot password,otp")
                .status(KnowledgeStatus.PUBLISHED)
                .build());

        knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .title("Troubleshooting Video Buffering on Android TV")
                .category("PLAYBACK")
                .content("If videos are buffering on Android TV, please verify your internet speed is above 15 Mbps, clear the Filmytell app cache in Android Settings > Apps > Filmytell > Clear Cache, and restart the TV.")
                .keywords("video,buffering,android tv,smart tv,streaming,playback")
                .status(KnowledgeStatus.PUBLISHED)
                .build());

        log.info("DataInitializer completed successfully.");
    }
}
