package tn.entreprise.escproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path profileDir = Paths.get("uploads/profile").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:" + profileDir.toString() + "/");

        Path filesDir = Paths.get("uploads/files").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/files/**")
                .addResourceLocations("file:" + filesDir.toString() + "/");
    }
}
