package com.chessgame.chessserver.controller.dto;

public class UserProfileDTO {
	private String username;
	private Integer elo;
	private Integer totalGames;
	private Integer wins;
	private Integer losses;
	private Integer draws;

	public UserProfileDTO() {}

	public UserProfileDTO(String username, Integer elo, Integer totalGames, Integer wins, Integer losses, Integer draws) {
		this.username = username;
		this.elo = elo;
		this.totalGames = totalGames;
		this.wins = wins;
		this.losses = losses;
		this.draws = draws;
	}

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public Integer getElo() { return elo; }
	public void setElo(Integer elo) { this.elo = elo; }
	public Integer getTotalGames() { return totalGames; }
	public void setTotalGames(Integer totalGames) { this.totalGames = totalGames; }
	public Integer getWins() { return wins; }
	public void setWins(Integer wins) { this.wins = wins; }
	public Integer getLosses() { return losses; }
	public void setLosses(Integer losses) { this.losses = losses; }
	public Integer getDraws() { return draws; }
	public void setDraws(Integer draws) { this.draws = draws; }
}


