package com.chessgame.chessserver.controller.dto;

import java.time.LocalDateTime;

public class MatchDTO {
	private Integer matchId;
	private String opponent;
	private String color;
	private String result;
	private Integer eloBefore;
	private Integer eloAfter;
	private LocalDateTime playedAt;

	public MatchDTO() {}

	public Integer getMatchId() { return matchId; }
	public void setMatchId(Integer matchId) { this.matchId = matchId; }
	public String getOpponent() { return opponent; }
	public void setOpponent(String opponent) { this.opponent = opponent; }
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
	public String getResult() { return result; }
	public void setResult(String result) { this.result = result; }
	public Integer getEloBefore() { return eloBefore; }
	public void setEloBefore(Integer eloBefore) { this.eloBefore = eloBefore; }
	public Integer getEloAfter() { return eloAfter; }
	public void setEloAfter(Integer eloAfter) { this.eloAfter = eloAfter; }
	public LocalDateTime getPlayedAt() { return playedAt; }
	public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}


