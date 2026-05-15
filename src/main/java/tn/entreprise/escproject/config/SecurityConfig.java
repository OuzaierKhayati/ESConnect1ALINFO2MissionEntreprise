package tn.entreprise.escproject.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import tn.entreprise.escproject.security.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtFilter jwtFilter;
    
    /**Configure password encoder using BCrypt @return BCryptPasswordEncoder bean*/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Get AuthenticationManager bean
     * Used for manual authentication in login endpoint
     * @param config - AuthenticationConfiguration
     * @return AuthenticationManager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Configure CORS settings
     * Allows requests from frontend
     * @return CorsConfigurationSource bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Main security filter chain configuration
     * Defines which endpoints are public and which are protected
     * @param http - HttpSecurity object
     * @return SecurityFilterChain bean
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints (context path /escproject/api is already stripped by servlet container)
                        .requestMatchers(HttpMethod.POST, "/user/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        
                        // Protected endpoints - require ROLE_RECRUITER
                        .requestMatchers(HttpMethod.POST, "/jobs").hasRole("RECRUITER")
                        .requestMatchers(HttpMethod.PUT, "/jobs/**").hasRole("RECRUITER")
                        .requestMatchers(HttpMethod.DELETE, "/jobs/**").hasRole("RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/jobs/my").hasRole("RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/applications/job/**").hasRole("RECRUITER")
                        .requestMatchers(HttpMethod.PUT, "/applications/*/status").hasRole("RECRUITER")
                        
                        // Protected endpoints - require ROLE_STUDENT
                        .requestMatchers(HttpMethod.POST, "/applications/job/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "/applications/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/applications/my").hasRole("STUDENT")

                        // Protected endpoints - require ROLE_ADMIN
                        .requestMatchers("/user/admin/**").hasRole("ADMIN")
                        // .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // Protected endpoints - any authenticated user
                        // .requestMatchers(HttpMethod.GET, "/**").authenticated()

                        // Notifications — any authenticated user
                        .requestMatchers("/notifications/**").authenticated()

                        // Allow messages and connections
                        .requestMatchers(HttpMethod.POST, "/messages/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/messages/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/messages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/messages/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/connections/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/connections/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/connections/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/connections/**").permitAll()
                        
                        // All other requests require authentication
                         .anyRequest().authenticated()
//                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized access. Please log in.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write("{\"success\":false,\"message\":\"Access denied. You do not have permission to access this resource.\"}");
                        })
                );
        
        return http.build();
    }
}