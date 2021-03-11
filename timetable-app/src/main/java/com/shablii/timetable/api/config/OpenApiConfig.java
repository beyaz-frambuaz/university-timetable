package com.shablii.timetable.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.*;

@Configuration
@OpenAPIDefinition
public class OpenApiConfig {

    @Bean
    public OpenAPI configureOpenApi(BuildProperties buildProperties) {

        return new OpenAPI().info(new Info().title("Timetable API")
                .version(buildProperties.getVersion())
                .contact(new Contact().name("Taras Shablii"))
                .description("Timetable App API spec"));
    }

}
