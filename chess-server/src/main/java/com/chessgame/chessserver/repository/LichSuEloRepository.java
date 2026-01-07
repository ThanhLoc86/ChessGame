package com.chessgame.chessserver.repository;

import com.chessgame.chessserver.domain.entity.LichSuElo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuEloRepository extends JpaRepository<LichSuElo, Integer> {
    java.util.Optional<LichSuElo> findByNguoiDungAndVanDau(com.chessgame.chessserver.domain.entity.NguoiDung nguoiDung, com.chessgame.chessserver.domain.entity.VanDau vanDau);
    java.util.List<LichSuElo> findByNguoiDungOrderByThoiGianDesc(com.chessgame.chessserver.domain.entity.NguoiDung nguoiDung);
}


