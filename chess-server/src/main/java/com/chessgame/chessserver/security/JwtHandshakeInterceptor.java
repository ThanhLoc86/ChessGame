package com.chessgame.chessserver.security;

import com.chessgame.chessserver.domain.entity.NguoiDung;
import com.chessgame.chessserver.repository.NguoiDungRepository;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final NguoiDungRepository nguoiDungRepository;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil, NguoiDungRepository nguoiDungRepository) {
        this.jwtUtil = jwtUtil;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (!(request instanceof ServletServerHttpRequest)) return false;
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String token = servletRequest.getParameter("token");
        if (token == null || token.isBlank()) return false;
        try {
            String username = jwtUtil.extractUsername(token);
            if (username == null) return false;
            if (!jwtUtil.validateToken(token, username)) return false;
            Optional<NguoiDung> o = nguoiDungRepository.findByTenDangNhap(username);
            if (o.isEmpty()) return false;
            attributes.put("user", o.get());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // noop
    }
}


