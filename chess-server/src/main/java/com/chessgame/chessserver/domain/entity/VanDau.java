package com.chessgame.chessserver.domain.entity;

import com.chessgame.chessserver.domain.enums.KetQuaVanDau;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "van_dau")
public class VanDau {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ma_van_dau")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ma_phong")
	private PhongChoi phongChoi;

	@Column(name = "thoi_gian_bat_dau")
	private LocalDateTime thoiGianBatDau;

	@Column(name = "thoi_gian_ket_thuc")
	private LocalDateTime thoiGianKetThuc;

	@Enumerated(EnumType.STRING)
	@Column(name = "ket_qua", length = 20)
	private KetQuaVanDau ketQua;

	public VanDau() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public PhongChoi getPhongChoi() {
		return phongChoi;
	}

	public void setPhongChoi(PhongChoi phongChoi) {
		this.phongChoi = phongChoi;
	}

	public LocalDateTime getThoiGianBatDau() {
		return thoiGianBatDau;
	}

	public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
		this.thoiGianBatDau = thoiGianBatDau;
	}

	public LocalDateTime getThoiGianKetThuc() {
		return thoiGianKetThuc;
	}

	public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
		this.thoiGianKetThuc = thoiGianKetThuc;
	}

	public KetQuaVanDau getKetQua() {
		return ketQua;
	}

	public void setKetQua(KetQuaVanDau ketQua) {
		this.ketQua = ketQua;
	}
}


