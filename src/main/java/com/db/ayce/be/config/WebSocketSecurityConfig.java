package com.db.ayce.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .nullDestMatcher().permitAll()
            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.HEARTBEAT).permitAll()
            // i client che scrivono su /app/** devono essere autenticati o validati dal ChannelInterceptor
            .simpDestMatchers("/app/**").authenticated()
            // solo admin/dipendenti possono sottoscrivere la cucina
            .simpSubscribeDestMatchers("/topic/cucina").hasAnyRole("ADMIN", "DIPEN")
            // room tavolo sono sottoscrivibili da tutti (ma i SEND saranno validati)
            .simpSubscribeDestMatchers("/topic/tavolo.*").permitAll()
            .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
