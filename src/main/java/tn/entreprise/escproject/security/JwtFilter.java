package tn.entreprise.escproject.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tn.entreprise.escproject.utils.JwtUtil;

@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    /**
     * Filter method called for each request
     * Validates JWT token and sets authentication context
     * @param request - HTTP request
     * @param response - HTTP response
     * @param filterChain - Filter chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Skip JWT validation for public endpoints
            String requestPath = request.getRequestURI();
            boolean isPublicEndpoint = requestPath.endsWith("/user/login") 
                    || requestPath.endsWith("/user/register")
                    || requestPath.contains("/swagger-ui") 
                    || requestPath.contains("/v3/api-docs")
                    || requestPath.contains("/oauth2/")
                    || requestPath.contains("/login/oauth2/");
            
            if (isPublicEndpoint) {
                filterChain.doFilter(request, response);
                return;
            }
            
            String jwt = extractTokenFromRequest(request);
            
            if (jwt != null) {
                // Extract email from token
                String email = jwtUtil.extractEmail(jwt);
                
                // Check if authentication is not already set (avoid duplicate processing)
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // Validate token
                    if (jwtUtil.validateToken(jwt, email)) {
                        // Load user details
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                        
                        // Create authentication token
                        UsernamePasswordAuthenticationToken authenticationToken = 
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        
                        // Set request details
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot set user authentication: " + ex.getMessage());
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer TOKEN"
     * @param request - HTTP request
     * @return JWT token or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}
