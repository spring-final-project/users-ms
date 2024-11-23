package com.springcloud.demo.usersmicroservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Users microservice",
                description = "Microservice to handle app users",
                version = "1.0.0",
                contact = @Contact(
                        name = "Gonzalo Jerez",
                        email = "gonzalojerezn@gmail.com",
                        url = "github.com/GonzaJerez"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Dev server",
                        variables = {

                        }
                )
        },
        security = @SecurityRequirement(name = "Authorization")
)
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        name = "Authorization"
)
public class DocConfig {}
