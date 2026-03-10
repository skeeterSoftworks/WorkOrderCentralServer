package com.skeeterSoftworks.WorkOrderCentral.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WorkOrderCentral API")
                        .version("v0.0.1")
                        .description("API documentation for WorkOrderCentral")
                        .contact(new Contact().name("Dev Team").email("dev@company.local"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
                );
    }
}

