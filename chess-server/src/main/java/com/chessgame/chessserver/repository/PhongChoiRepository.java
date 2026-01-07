package com.chessgame.chessserver.repository;

import com.chessgame.chessserver.domain.entity.PhongChoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongChoiRepository extends JpaRepository<PhongChoi, Integer> {
}


