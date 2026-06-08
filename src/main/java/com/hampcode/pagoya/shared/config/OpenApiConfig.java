package com.hampcode.pagoya.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI pagoyaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PagoYa API")
                .description("API de la billetera digital PagoYa")
                .version("v1")
                .contact(new Contact()
                    .name("Equipo PagoYa by HampCode")
                    .email("devacademyweb@gmail.com")))
            .addSecurityItem(new SecurityRequirement().addList(SCHEME))
            .components(new Components().addSecuritySchemes(SCHEME,
                new SecurityScheme()
                    .name(SCHEME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
