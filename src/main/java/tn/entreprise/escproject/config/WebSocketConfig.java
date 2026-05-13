package tn.entreprise.escproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker //activate the WebSocket, STOMP et broker Messages.
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker( MessageBrokerRegistry registry) {
                // cree le canaux "/topic"
                // le client envoi vers spring boot.
        registry.enableSimpleBroker("/topic", "/queue");
                // spring boot diffuse au client (front)
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {
        // addEndpoint("/chat") : Endpoint WebSocket principal => ws://localhost:8083/chat
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}