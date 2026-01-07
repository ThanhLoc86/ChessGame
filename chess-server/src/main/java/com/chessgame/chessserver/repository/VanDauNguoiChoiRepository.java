package com.chessgame.chessserver.repository;

import com.chessgame.chessserver.domain.entity.VanDauNguoiChoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VanDauNguoiChoiRepository extends JpaRepository<VanDauNguoiChoi, Integer> {
    java.util.List<VanDauNguoiChoi> findByNguoiDungOrderByIdDesc(com.chessgame.chessserver.domain.entity.NguoiDung nguoiDung);
    java.util.List<VanDauNguoiChoi> findByVanDauAndNguoiDungNot(com.chessgame.chessserver.domain.entity.VanDau vanDau, com.chessgame.chessserver.domain.entity.NguoiDung nguoiDung);
}


