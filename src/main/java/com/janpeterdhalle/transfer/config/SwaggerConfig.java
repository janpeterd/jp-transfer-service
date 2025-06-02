package com.janpeterdhalle.transfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
    private static final String SECURITY_SHEME_NAME = "security";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SHEME_NAME))
                .components(
                        new Components()
                                .addSecuritySchemes(SECURITY_SHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")

                                ))
                .info(new Info()
                        .title("JP Transfer API"));
    }
}
