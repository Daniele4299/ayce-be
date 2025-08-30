package com.db.ayce.be.config;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.db.ayce.be.security.jwt.JwtService;

import io.jsonwebtoken.Claims;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() != null && accessor.getCommand().name().equals("CONNECT")) {
            // Legge il token dal header Authorization se presente
            String token = accessor.getFirstNativeHeader("Authorization");

            // Se non c'è, prova dai cookie (usando "cookie" header)
            if (token == null) {
                String cookieHeader = accessor.getFirstNativeHeader("cookie");
                if (cookieHeader != null) {
                    for (String c : cookieHeader.split(";")) {
                        String[] kv = c.trim().split("=");
                        if (kv.length == 2 && kv[0].equals("token")) {
                            token = kv[1];
                            break;
                        }
                    }
                }
            }

            if (token != null) {
                try {
                    Claims claims = jwtService.extractAllClaims(token);
                    UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(claims, null, List.of());
                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    System.out.println("Token non valido: " + e.getMessage());
                }
            }
        }

        return message;
    }
}
