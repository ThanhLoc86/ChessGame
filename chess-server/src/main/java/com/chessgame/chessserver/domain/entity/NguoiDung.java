package com.chessgame.chessserver.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
public class NguoiDung {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ma_nguoi_dung")
	private Integer id;

	@Column(name = "ten_dang_nhap", nullable = false, unique = true, length = 100)
	private String tenDangNhap;

	@Column(name = "mat_khau_hash", nullable = false, length = 255)
	private String matKhauHash;

	@Column(name = "diem_elo", nullable = false)
	private Integer diemElo = 1200;

	@Column(name = "ngay_tao", nullable = false)
	private LocalDateTime ngayTao;

	public NguoiDung() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTenDangNhap() {
		return tenDangNhap;
	}

	public void setTenDangNhap(String tenDangNhap) {
		this.tenDangNhap = tenDangNhap;
	}

	public String getMatKhauHash() {
		return matKhauHash;
	}

	public void setMatKhauHash(String matKhauHash) {
		this.matKhauHash = matKhauHash;
	}

	public Integer getDiemElo() {
		return diemElo;
	}

	public void setDiemElo(Integer diemElo) {
		this.diemElo = diemElo;
	}

	public LocalDateTime getNgayTao() {
		return ngayTao;
	}

	public void setNgayTao(LocalDateTime ngayTao) {
		this.ngayTao = ngayTao;
	}
}

