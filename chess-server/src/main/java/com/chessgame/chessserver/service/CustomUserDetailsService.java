package com.chessgame.chessserver.service;

import com.chessgame.chessserver.domain.entity.NguoiDung;
import com.chessgame.chessserver.repository.NguoiDungRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;

    public CustomUserDetailsService(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<NguoiDung> o = nguoiDungRepository.findByTenDangNhap(username);
        NguoiDung nd = o.orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(nd.getTenDangNhap()).password(nd.getMatKhauHash()).authorities("USER").build();
    }
}


