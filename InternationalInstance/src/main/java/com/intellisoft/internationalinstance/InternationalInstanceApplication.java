package com.intellisoft.internationalinstance;

import com.itextpdf.text.FontFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@Log4j2
@SpringBootApplication
@PropertySources({@PropertySource("classpath:.env"), @PropertySource("classpath:application.properties")})
public class InternationalInstanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InternationalInstanceApplication.class, args);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public void registerFonts() {
        try {
            File fontFile = ResourceUtils.getFile("classpath:fonts/GILLSANS.ttf");
            FontFactory.register(fontFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error occurred, font file not found");
        }
    }

}
