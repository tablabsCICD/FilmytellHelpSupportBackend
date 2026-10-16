package com.filmytell.support.security;

import com.filmytell.support.entity.PortalApiKey;
import com.filmytell.support.enums.SourcePortal;
import com.filmytell.support.repository.PortalApiKeyRepository;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final PortalApiKeyRepository portalApiKeyRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/external")) {
            String apiKey = request.getHeader("X-API-KEY");
            String portalIdHeader = request.getHeader("X-PORTAL-ID");

            if (StringUtils.hasText(apiKey)) {
                Optional<PortalApiKey> keyOpt;
                if (StringUtils.hasText(portalIdHeader)) {
                    try {
                        SourcePortal portal = SourcePortal.valueOf(portalIdHeader.trim().toUpperCase());
                        keyOpt = portalApiKeyRepository.findByPortalNameAndApiKeyHashAndActiveTrue(portal, apiKey);
                    } catch (IllegalArgumentException e) {
                        keyOpt = portalApiKeyRepository.findByApiKeyHashAndActiveTrue(apiKey);
                    }
                } else {
                    keyOpt = portalApiKeyRepository.findByApiKeyHashAndActiveTrue(apiKey);
                }

                if (keyOpt.isPresent()) {
                    PortalApiKey portalKey = keyOpt.get();
                    String roleName = "ROLE_EXTERNAL_PORTAL_" + portalKey.getPortalName().name();

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            portalKey.getPortalName().name(),
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(roleName))
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("Invalid or inactive API Key attempted for portal request: {}", request.getRemoteAddr());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
