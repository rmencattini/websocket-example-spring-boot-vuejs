package com.poc.websocket.config;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.poc.websocket.entity.User;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/alert");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/websocket")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new HttpHandshakeInterceptor());
    }

    public class HttpHandshakeInterceptor implements ChannelInterceptor {

        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        String jwkSetUri;

        @Override
        public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message,
                MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (StompCommand.SEND == accessor.getCommand()) {
                JwtDecoder jwtDecoder = jwtDecoder();
                String authorizationToken = accessor.getFirstNativeHeader("Authorization");
                if (authorizationToken != null) {
                    String token = authorizationToken.substring(7);
                    Jwt jwt = jwtDecoder.decode(token);
                    String username = jwt.getClaimAsString("preferred_username");
                    Principal principal = User.builder().name(username).build();
                    accessor.setUser(principal);
                } else {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error("invalid_token", "Missing access token", null));
                }
            }
            return message;
        }

        JwtDecoder jwtDecoder() {
            return NimbusJwtDecoder
                    .withJwkSetUri("http://localhost:8080/auth/realms/dummy/protocol/openid-connect/certs").build();
        }
    }
}
