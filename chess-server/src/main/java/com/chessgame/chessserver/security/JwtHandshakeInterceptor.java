package com.chessgame.chessserver.security;

import com.chessgame.chessserver.domain.entity.NguoiDung;
import com.chessgame.chessserver.repository.NguoiDungRepository;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JwtUtil jwtUtil;
    private final NguoiDungRepository nguoiDungRepository;
    private final UserDetailsService userDetailsService;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil,
                                   NguoiDungRepository nguoiDungRepository,
                                   UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.nguoiDungRepository = nguoiDungRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                ServerHttpResponse response,
                                WebSocketHandler wsHandler,
                                Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest)) {
            logger.info("WS handshake rejected: not a ServletServerHttpRequest");
            return false;
        }

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String token = servletRequest.getParameter("token");
        if (token == null || token.isBlank()) {
            logger.info("WS handshake rejected: missing token");
            return false;
        }

        // truncate token for logging (avoid leaking full token)
        String tokenPreview = token.length() > 16 ? token.substring(0, 16) + "..." : token;

        try {
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                logger.info("WS handshake rejected: username extraction failed (token preview={})", tokenPreview);
                return false;
            }

            // load user details (may throw UsernameNotFoundException)
            UserDetails ud;
            try {
                ud = userDetailsService.loadUserByUsername(username);
            } catch (Exception ex) {
                logger.info("WS handshake rejected: userDetailsService failed for username={} (token preview={})", username, tokenPreview);
                return false;
            }

            if (!jwtUtil.validateToken(token, username)) {
                logger.info("WS handshake rejected: token validation failed for username={} (token preview={})", username, tokenPreview);
                return false;
            }

            Optional<NguoiDung> o = nguoiDungRepository.findByTenDangNhap(username);
            if (o.isEmpty()) {
                logger.info("WS handshake rejected: user not found in DB username={} (token preview={})", username, tokenPreview);
                return false;
            }

            attributes.put("user", o.get());
            logger.info("WS handshake accepted for username={}", username);
            return true;

        } catch (Exception ex) {
            logger.error("WS handshake error (token preview={})", tokenPreview, ex);
            return false;
        }
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        
    }
}


