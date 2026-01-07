package com.chessgame.chessserver.service;

import org.springframework.stereotype.Service;

/**
 * Elo calculation utility service.
 * Uses standard Elo formula with fixed K-factor = 32.
 *
 * This service is stateless and does not access any database.
 */
@Service
public class EloService {

	private static final int K_FACTOR = 32;

	/**
	 * Calculate new Elo rating for a player after a game.
	 *
	 * @param eloHienTai current rating of the player
	 * @param eloDoiThu  rating of the opponent
	 * @param ketQua     result: 1.0 = win, 0.5 = draw, 0.0 = loss
	 * @return new Elo rating (rounded)
	 */
	public int calculateNewElo(int eloHienTai, int eloDoiThu, double ketQua) {
		double expected = 1.0 / (1.0 + Math.pow(10.0, (eloDoiThu - eloHienTai) / 400.0));
		double updated = eloHienTai + K_FACTOR * (ketQua - expected);
		return (int) Math.round(updated);
	}
}


