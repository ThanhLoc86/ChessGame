package com.chessgame.chessserver.domain.entity;

import com.chessgame.chessserver.domain.enums.TrangThaiPhong;
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
@Table(name = "phong_choi")
public class PhongChoi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ma_phong")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ma_chu_phong", nullable = false)
	private NguoiDung chuPhong;

	@Enumerated(EnumType.STRING)
	@Column(name = "trang_thai", nullable = false, length = 20)
	private TrangThaiPhong trangThai;

	@Column(name = "thoi_gian_tao", nullable = false)
	private LocalDateTime thoiGianTao;

	public PhongChoi() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public NguoiDung getChuPhong() {
		return chuPhong;
	}

	public void setChuPhong(NguoiDung chuPhong) {
		this.chuPhong = chuPhong;
	}

	public TrangThaiPhong getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(TrangThaiPhong trangThai) {
		this.trangThai = trangThai;
	}

	public LocalDateTime getThoiGianTao() {
		return thoiGianTao;
	}

	public void setThoiGianTao(LocalDateTime thoiGianTao) {
		this.thoiGianTao = thoiGianTao;
	}
}




