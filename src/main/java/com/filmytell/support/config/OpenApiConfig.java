package com.filmytell.support.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String bearerAuth = "Bearer Authentication";
        final String apiKeyAuth = "Portal API Key";

        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/help-support-backend").description("Tomcat Deployment Context Path"),
                        new Server().url("/").description("Default Root Context Path")
                ))
                .info(new Info()
                        .title("Filmytell Help Support System API")
                        .description("Centralized Help Support Backend service managing support tickets from Filmytell, Filmytell Partner, and Production House portals with multi-level delegation, Kanban, SLA, and AI support.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Filmytell Engineering")
                                .email("support-tech@filmytell.com"))
                        .license(new License().name("Proprietary").url("https://filmytell.com")))
                .addSecurityItem(new SecurityRequirement().addList(bearerAuth).addList(apiKeyAuth))
                .components(new Components()
                        .addSecuritySchemes(bearerAuth, new SecurityScheme()
                                .name(bearerAuth)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token in the format: Bearer <token>"))
                        .addSecuritySchemes(apiKeyAuth, new SecurityScheme()
                                .name("X-API-KEY")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Enter Portal API Key for external portal ingestion")));
    }
}
