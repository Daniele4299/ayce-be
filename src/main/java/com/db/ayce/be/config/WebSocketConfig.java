package com.db.ayce.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Broker semplice in memoria con /topic per le subscription
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // destinazioni broadcast
        config.setApplicationDestinationPrefixes("/app"); // destinazioni in ingresso dai client
    }

    // Endpoint HTTP/WS a cui i client si connettono
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                 // wss://host/ws
                .setAllowedOriginPatterns("*")      // regola in produzione con il tuo dominio
                .withSockJS();                      // fallback per vecchi browser/proxy
    }
}
