package com.example.member.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";
    @Value("${spring.application.name}")
    private String appName;
    @Value("${apigateway.host}")
    private String gatewayHost;

    @Bean
    public OpenAPI openAPI() {
        // Server server = new Server();
        // server.url(String.format("%s/%s", gatewayHost, appName)); // http://localhost:8000/member-service
        // List<Server> servers = new ArrayList<>();
        // servers.add(server);
        //
        // SecurityScheme bearerScheme = new SecurityScheme()
        //     .name("Authorization")
        //     .type(SecurityScheme.Type.HTTP)
        //     .scheme("bearer")
        //     .bearerFormat("JWT")
        //     .in(SecurityScheme.In.HEADER);

        return new OpenAPI().info(new Info()
                            .title("Member Service API")
                            .version("v1"));
            // .servers(servers)
            // .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme))
            // .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
