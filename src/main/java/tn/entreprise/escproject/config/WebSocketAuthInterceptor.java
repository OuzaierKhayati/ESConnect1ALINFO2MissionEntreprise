package tn.entreprise.escproject.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import tn.entreprise.escproject.security.CustomUserDetailsService;
import tn.entreprise.escproject.utils.JwtUtil;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String email = jwtUtil.extractEmail(token);
                    if (email != null && jwtUtil.validateToken(token, email)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(auth);
                        log.info("WebSocket authenticated user: {}", email);
                    } else {
                        log.warn("WebSocket JWT validation failed for token");
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication error: {}", e.getMessage());
                }
            } else {
                log.warn("WebSocket CONNECT without Authorization header");
            }
        }

        return message;
    }
}
