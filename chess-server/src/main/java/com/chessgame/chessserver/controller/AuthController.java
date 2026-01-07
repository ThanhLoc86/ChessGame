package com.chessgame.chessserver.controller;

import com.chessgame.chessserver.domain.entity.NguoiDung;
import com.chessgame.chessserver.repository.NguoiDungRepository;
import com.chessgame.chessserver.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(NguoiDungRepository nguoiDungRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "username_and_password_required"));
        }
        if (nguoiDungRepository.findByTenDangNhap(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "username_exists"));
        }
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(username);
        nd.setMatKhauHash(passwordEncoder.encode(password));
        nd.setDiemElo(1200);
        nd.setNgayTao(java.time.LocalDateTime.now());
        nguoiDungRepository.save(nd);
        return ResponseEntity.status(201).body(Map.of("message", "register_success"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "username_and_password_required"));
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid_credentials"));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "authentication_failed"));
        }
        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of("token", token, "username", username));
    }
}


