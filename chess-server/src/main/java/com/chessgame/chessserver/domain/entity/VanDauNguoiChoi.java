package com.chessgame.chessserver.domain.entity;

import com.chessgame.chessserver.domain.enums.KetQuaNguoiChoi;
import com.chessgame.chessserver.domain.enums.MauCo;
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

@Entity
@Table(name = "van_dau_nguoi_choi")
public class VanDauNguoiChoi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ma")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ma_van_dau", nullable = false)
	private VanDau vanDau;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ma_nguoi_dung", nullable = false)
	private NguoiDung nguoiDung;

	@Enumerated(EnumType.STRING)
	@Column(name = "mau_co", length = 10, nullable = false)
	private MauCo mauCo;

	@Enumerated(EnumType.STRING)
	@Column(name = "ket_qua", length = 10)
	private KetQuaNguoiChoi ketQua;

	public VanDauNguoiChoi() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public VanDau getVanDau() {
		return vanDau;
	}

	public void setVanDau(VanDau vanDau) {
		this.vanDau = vanDau;
	}

	public NguoiDung getNguoiDung() {
		return nguoiDung;
	}

	public void setNguoiDung(NguoiDung nguoiDung) {
		this.nguoiDung = nguoiDung;
	}

	public MauCo getMauCo() {
		return mauCo;
	}

	public void setMauCo(MauCo mauCo) {
		this.mauCo = mauCo;
	}

	public KetQuaNguoiChoi getKetQua() {
		return ketQua;
	}

	public void setKetQua(KetQuaNguoiChoi ketQua) {
		this.ketQua = ketQua;
	}
}



