package com.chessgame.chessserver.repository;

import com.chessgame.chessserver.domain.entity.NguoiDung;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
	Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
}


