package com.chessgame.chessserver.repository;

import com.chessgame.chessserver.domain.entity.VanDau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VanDauRepository extends JpaRepository<VanDau, Integer> {
}


