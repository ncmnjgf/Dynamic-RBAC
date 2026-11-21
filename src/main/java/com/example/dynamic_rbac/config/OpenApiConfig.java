package com.example.dynamic_rbac.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Enter HTTP Basic Authentication credentials (e.g. admin / admin123 or user / user123)"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dynamic RBAC Authorization Engine API")
                        .version("1.0.0")
                        .description("API Documentation for Dynamic Role-Based Access Control (RBAC) Engine. " +
                                "Endpoints are dynamically protected using a custom PermissionEvaluator against database-stored roles and permissions.")
                        .contact(new Contact().name("TechPulse IT Assessment")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
