package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker

public class WebSocketConfig implements WebSocketMessageBrokerConfigurer  {
	 @Override
	    public void configureMessageBroker(MessageBrokerRegistry config) {
	        // Enbound channels: client subscribes to get messages
	        // /topic is for broadcasts/groups, /queue is for 1-to-1 private messaging
	        config.enableSimpleBroker("/topic", "/queue");
	        
	        // Outbound channels: messages sent from client to controllers
	        config.setApplicationDestinationPrefixes("/app");
	        
	        // Prefix for targeting individual user queues
	        config.setUserDestinationPrefix("/user");
	    }
	    @Override
	    public void registerStompEndpoints(StompEndpointRegistry registry) {
	        registry.addEndpoint("/ws-chat")
	                .setAllowedOriginPatterns("*")
	                .withSockJS(); // SockJS fallback for older browsers
	    }

}
