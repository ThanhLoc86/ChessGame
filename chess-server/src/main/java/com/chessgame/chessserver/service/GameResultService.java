package com.chessgame.chessserver.service;

import com.chessgame.chessserver.domain.entity.LichSuElo;
import com.chessgame.chessserver.domain.entity.NguoiDung;
import com.chessgame.chessserver.domain.entity.VanDau;
import com.chessgame.chessserver.domain.entity.VanDauNguoiChoi;
import com.chessgame.chessserver.domain.enums.KetQuaNguoiChoi;
import com.chessgame.chessserver.domain.enums.KetQuaVanDau;
import com.chessgame.chessserver.domain.enums.MauCo;
import com.chessgame.chessserver.repository.LichSuEloRepository;
import com.chessgame.chessserver.repository.NguoiDungRepository;
import com.chessgame.chessserver.repository.VanDauNguoiChoiRepository;
import com.chessgame.chessserver.repository.VanDauRepository;
import com.chessgame.chessserver.game.GameSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Service to persist final game results. This is NOT realtime and should be called once per finished game.
 */
@Service
@Transactional
public class GameResultService {

	private final VanDauRepository vanDauRepository;
	private final VanDauNguoiChoiRepository vanDauNguoiChoiRepository;
	private final NguoiDungRepository nguoiDungRepository;
	private final LichSuEloRepository lichSuEloRepository;

	public GameResultService(VanDauRepository vanDauRepository,
							 VanDauNguoiChoiRepository vanDauNguoiChoiRepository,
							 NguoiDungRepository nguoiDungRepository,
							 LichSuEloRepository lichSuEloRepository) {
		this.vanDauRepository = vanDauRepository;
		this.vanDauNguoiChoiRepository = vanDauNguoiChoiRepository;
		this.nguoiDungRepository = nguoiDungRepository;
		this.lichSuEloRepository = lichSuEloRepository;
	}

	/**
	 * Persist the finished game.
	 * Expects GameSession to contain the domain players (white/black), start/end times and result.
	 * This method does not access WebSocketSession or use reflection.
	 */
	public void saveResult(GameSession gameSession) {
		Objects.requireNonNull(gameSession, "gameSession must not be null");
		NguoiDung white = gameSession.getWhitePlayer();
		NguoiDung black = gameSession.getBlackPlayer();

		if (white == null || black == null) {
			throw new IllegalArgumentException("Both players must be present and authenticated in GameSession");
		}

		// times: try reflective getters on GameSession, else fallback to now
		LocalDateTime start = gameSession.getStartTime();
		LocalDateTime end = gameSession.getEndTime();
		if (start == null) start = LocalDateTime.now();
		if (end == null) end = LocalDateTime.now();

		// result: read typed enum
		var resultEnum = gameSession.getResult();
		if (resultEnum == null) {
			throw new IllegalArgumentException("Game result not available in GameSession");
		}
		String resultRaw = resultEnum.name();

		// Map to enums used by entities
		KetQuaVanDau vanResult = mapToVanResult(resultRaw);

		// Persist VanDau
		VanDau vd = new VanDau();
		vd.setPhongChoi(null); // not tracked here
		vd.setThoiGianBatDau(start);
		vd.setThoiGianKetThuc(end);
		vd.setKetQua(vanResult);
		vanDauRepository.save(vd);

		// Determine player results
		KetQuaNguoiChoi whiteRes = computePlayerResult(resultRaw, true);
		KetQuaNguoiChoi blackRes = computePlayerResult(resultRaw, false);

		// ELO calculation
		int eloWhiteBefore = Optional.ofNullable(white.getDiemElo()).orElse(1200);
		int eloBlackBefore = Optional.ofNullable(black.getDiemElo()).orElse(1200);
		int[] newElos = calculateElo(eloWhiteBefore, eloBlackBefore, whiteRes, blackRes);
		int eloWhiteAfter = newElos[0];
		int eloBlackAfter = newElos[1];

		// Update users
		white.setDiemElo(eloWhiteAfter);
		black.setDiemElo(eloBlackAfter);
		nguoiDungRepository.save(white);
		nguoiDungRepository.save(black);

		// Persist VanDauNguoiChoi records
		VanDauNguoiChoi vwn = new VanDauNguoiChoi();
		vwn.setVanDau(vd);
		vwn.setNguoiDung(white);
		vwn.setMauCo(MauCo.TRANG);
		vwn.setKetQua(whiteRes);
		vanDauNguoiChoiRepository.save(vwn);

		VanDauNguoiChoi vbn = new VanDauNguoiChoi();
		vbn.setVanDau(vd);
		vbn.setNguoiDung(black);
		vbn.setMauCo(MauCo.DEN);
		vbn.setKetQua(blackRes);
		vanDauNguoiChoiRepository.save(vbn);

		// Persist Elo history
		LichSuElo l1 = new LichSuElo();
		l1.setNguoiDung(white);
		l1.setVanDau(vd);
		l1.setEloTruoc(eloWhiteBefore);
		l1.setEloSau(eloWhiteAfter);
		l1.setThoiGian(end);
		lichSuEloRepository.save(l1);

		LichSuElo l2 = new LichSuElo();
		l2.setNguoiDung(black);
		l2.setVanDau(vd);
		l2.setEloTruoc(eloBlackBefore);
		l2.setEloSau(eloBlackAfter);
		l2.setThoiGian(end);
		lichSuEloRepository.save(l2);
	}

	// legacy helpers removed — GameSession provides typed getters now

	private KetQuaVanDau mapToVanResult(String raw) {
		if (raw == null) return KetQuaVanDau.HOA;
		switch (raw.toUpperCase()) {
			case "WHITE_WIN", "TRANG_THANG", "TRANG" -> {
				return KetQuaVanDau.TRANG_THANG;
			}
			case "BLACK_WIN", "DEN_THANG", "DEN" -> {
				return KetQuaVanDau.DEN_THANG;
			}
			default -> {
				return KetQuaVanDau.HOA;
			}
		}
	}

	private KetQuaNguoiChoi computePlayerResult(String raw, boolean isWhite) {
		if (raw == null) return KetQuaNguoiChoi.HOA;
		switch (raw.toUpperCase()) {
			case "WHITE_WIN", "TRANG_THANG" -> {
				return isWhite ? KetQuaNguoiChoi.THANG : KetQuaNguoiChoi.THUA;
			}
			case "BLACK_WIN", "DEN_THANG" -> {
				return isWhite ? KetQuaNguoiChoi.THUA : KetQuaNguoiChoi.THANG;
			}
			default -> {
				return KetQuaNguoiChoi.HOA;
			}
		}
	}

	/**
	 * Simple ELO calculator. K-factor fixed to 32.
	 * Returns int[]{newWhiteElo, newBlackElo}
	 */
	private int[] calculateElo(int eloWhite, int eloBlack, KetQuaNguoiChoi whiteRes, KetQuaNguoiChoi blackRes) {
		double scoreWhite = resultToScore(whiteRes);
		double scoreBlack = resultToScore(blackRes);
		double expectedWhite = 1.0 / (1.0 + Math.pow(10.0, (eloBlack - eloWhite) / 400.0));
		double expectedBlack = 1.0 / (1.0 + Math.pow(10.0, (eloWhite - eloBlack) / 400.0));
		int K = 32;
		int newWhite = (int) Math.round(eloWhite + K * (scoreWhite - expectedWhite));
		int newBlack = (int) Math.round(eloBlack + K * (scoreBlack - expectedBlack));
		return new int[] { newWhite, newBlack };
	}

	private double resultToScore(KetQuaNguoiChoi res) {
		return switch (res) {
			case THANG -> 1.0;
			case HOA -> 0.5;
			default -> 0.0;
		};
	}
}


