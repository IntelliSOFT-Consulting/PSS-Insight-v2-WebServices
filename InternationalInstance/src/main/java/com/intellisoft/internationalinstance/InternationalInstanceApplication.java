package com.intellisoft.internationalinstance;

import com.itextpdf.text.FontFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class InternationalInstanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternationalInstanceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public void registerFonts() {
        try {
            File fontFile = ResourceUtils.getFile("classpath:fonts/GILLSANS.ttf");
            // Register the custom font using the font file path
            FontFactory.register(fontFile.getAbsolutePath());
            System.out.println(fontFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
