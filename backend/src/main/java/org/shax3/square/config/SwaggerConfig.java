package org.shax3.square.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String JWT_SCHEME_NAME = "JWT";
    public static final String COOKIE_SCHEME_NAME = "refresh-token";
    public static final String BEARER_SCHEME = "Bearer";

    @Bean
    public OpenAPI openAPI() {

        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name(JWT_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme(BEARER_SCHEME);

        SecurityScheme cookieSecurityScheme = new SecurityScheme()
                .name(COOKIE_SCHEME_NAME)
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE);

        return new OpenAPI()
            .components(new Components()
                    .addSecuritySchemes(JWT_SCHEME_NAME, jwtSecurityScheme)
                    .addSecuritySchemes(COOKIE_SCHEME_NAME, cookieSecurityScheme))
            .addSecurityItem(new SecurityRequirement()
                    .addList(JWT_SCHEME_NAME)
                    .addList(COOKIE_SCHEME_NAME));
    }
}
