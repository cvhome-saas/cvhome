package com.asrevo.cvhome.domaincertificatemanager.config;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodId;
import com.asrevo.cvhome.domaincertificatemanager.entity.FileId;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    static {
        SpringDocUtils.getConfig().replaceWithClass(CertificateId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(DomainId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(OrdersId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(EventId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(IdentityId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(FileId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(PodId.class, String.class);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        String name = "bearer-key";
        String basePath = "/";
//    if (Set.of(environment.getActiveProfiles()).contains("cloud")) {
//      basePath = "/uxplore";
//    }
        Server server = new Server()
                .url(basePath);
        SecurityScheme securitySchemesItem = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT");
        Components components = new Components().addSecuritySchemes(name, securitySchemesItem);
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(name);
        Info info = new Info()
                .title("title")
                .version("1.0");
        return new OpenAPI()
                .addServersItem(server)
                .components(components)
                .addSecurityItem(securityRequirement)
                .info(info);
    }
}
