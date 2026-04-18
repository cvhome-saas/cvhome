package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@ConditionalOnClass(OpenAPI.class)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String name = "bearer-key";
        String basePath = "/";
        Server server = new Server().url(basePath);
        SecurityScheme securitySchemesItem = new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT");
        Components components = new Components().addSecuritySchemes(name, securitySchemesItem);
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(name);
        Info info = new Info().title("title").version("1.0");
        return new OpenAPI().addServersItem(server)
                .components(components)
                .addSecurityItem(securityRequirement)
                .info(info);
    }

}
