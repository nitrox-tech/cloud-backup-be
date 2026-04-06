package com.nitro.tech.cloud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SCHEME_API_KEY = "apiKey";
    public static final String SCHEME_BEARER_JWT = "bearer-jwt";

    @Bean
    OpenAPI openAPI(ApiKeyProperties apiKeyProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title("1cloud-be API")
                        .version("1.0")
                        .description(
                                "Protected routes need **API Key** (header) and **Bearer JWT** (from POST /auth/telegram). "
                                        + "Use **Authorize** and fill both, then Try it out."))
                .components(new Components()
                        .addSecuritySchemes(
                                SCHEME_BEARER_JWT,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Value from POST /auth/telegram response field `token` (paste without Bearer prefix in the Authorize dialog)."))
                        .addSecuritySchemes(
                                SCHEME_API_KEY,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(apiKeyProperties.getHeaderName())
                                        .description("Must match app API key (e.g. X-API-Key)")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SCHEME_API_KEY)
                        .addList(SCHEME_BEARER_JWT));
    }
}
