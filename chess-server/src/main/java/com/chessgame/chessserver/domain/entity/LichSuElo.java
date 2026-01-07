package com.chessgame.chessserver.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_elo")
public class LichSuElo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ma")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ma_nguoi_dung", nullable = false)
	private NguoiDung nguoiDung;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ma_van_dau", nullable = false)
	private VanDau vanDau;

	@Column(name = "elo_truoc", nullable = false)
	private Integer eloTruoc;

	@Column(name = "elo_sau", nullable = false)
	private Integer eloSau;

	@Column(name = "thoi_gian", nullable = false)
	private LocalDateTime thoiGian;

	public LichSuElo() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public NguoiDung getNguoiDung() {
		return nguoiDung;
	}

	public void setNguoiDung(NguoiDung nguoiDung) {
		this.nguoiDung = nguoiDung;
	}

	public VanDau getVanDau() {
		return vanDau;
	}

	public void setVanDau(VanDau vanDau) {
		this.vanDau = vanDau;
	}

	public Integer getEloTruoc() {
		return eloTruoc;
	}

	public void setEloTruoc(Integer eloTruoc) {
		this.eloTruoc = eloTruoc;
	}

	public Integer getEloSau() {
		return eloSau;
	}

	public void setEloSau(Integer eloSau) {
		this.eloSau = eloSau;
	}

	public LocalDateTime getThoiGian() {
		return thoiGian;
	}

	public void setThoiGian(LocalDateTime thoiGian) {
		this.thoiGian = thoiGian;
	}
}

