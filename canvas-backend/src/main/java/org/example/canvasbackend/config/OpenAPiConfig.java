package org.example.canvasbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPiConfig {

    @Bean
    public OpenAPI canvasService() {

        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Canvas Backend API")
                        .description("API Documentation")
                        .version("0.0.1")
                        .contact(new Contact())
                );
    }
}
