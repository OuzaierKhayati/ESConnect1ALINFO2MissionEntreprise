package tn.entreprise.escproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        String uploadDir = Paths.get("uploads").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/image/**")
                .addResourceLocations(uploadDir + "image/");

        registry.addResourceHandler("/video/**")
                .addResourceLocations(uploadDir + "video/");
    }
}
