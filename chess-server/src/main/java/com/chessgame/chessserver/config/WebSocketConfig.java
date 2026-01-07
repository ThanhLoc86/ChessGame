package com.chessgame.chessserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.chessgame.chessserver.security.JwtHandshakeInterceptor;

/**
 * Registers the WebSocket endpoint used by the game frontend.
 * Configures allowed origins from environment variables for security.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final WebSocketHandler chessWebSocketHandler;
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	@Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
	private String[] allowedOrigins;

	public WebSocketConfig(WebSocketHandler chessWebSocketHandler, JwtHandshakeInterceptor jwtHandshakeInterceptor) {
		this.chessWebSocketHandler = chessWebSocketHandler;
		this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chessWebSocketHandler, "/ws/game")
				.addInterceptors(jwtHandshakeInterceptor)
				.setAllowedOriginPatterns(
					"http://localhost:5173",
					"https://chessgame-production-*.up.railway.app"
				);
	}

}


