package com.intellisoft.pssnationalinstance.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class EnvConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocations(
                new ClassPathResource("application.properties"),  // Load properties from application.properties
                new FileSystemResource(".env") // Load properties from .env
        );
        configurer.setIgnoreResourceNotFound(true);  // Ignore if one of the resources is not found
        return configurer;
    }
}

