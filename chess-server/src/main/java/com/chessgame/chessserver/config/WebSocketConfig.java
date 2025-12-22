package com.chessgame.chessserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers the WebSocket endpoint used by the game frontend.
 * Allows all origins (development) and delegates handling to a WebSocketHandler bean.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final WebSocketHandler chessWebSocketHandler;

	public WebSocketConfig(WebSocketHandler chessWebSocketHandler) {
		this.chessWebSocketHandler = chessWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chessWebSocketHandler, "/ws/game").setAllowedOrigins("*");
	}
}


