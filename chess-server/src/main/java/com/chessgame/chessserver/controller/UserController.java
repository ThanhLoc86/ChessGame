package com.chessgame.chessserver.controller;

import com.chessgame.chessserver.controller.dto.MatchDTO;
import com.chessgame.chessserver.controller.dto.UserProfileDTO;
import com.chessgame.chessserver.domain.entity.LichSuElo;
import com.chessgame.chessserver.domain.entity.NguoiDung;
import com.chessgame.chessserver.domain.entity.VanDau;
import com.chessgame.chessserver.domain.entity.VanDauNguoiChoi;
import com.chessgame.chessserver.repository.LichSuEloRepository;
import com.chessgame.chessserver.repository.NguoiDungRepository;
import com.chessgame.chessserver.repository.VanDauNguoiChoiRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final NguoiDungRepository nguoiDungRepository;
    private final VanDauNguoiChoiRepository vanDauNguoiChoiRepository;
    private final LichSuEloRepository lichSuEloRepository;

    public UserController(NguoiDungRepository nguoiDungRepository,
                          VanDauNguoiChoiRepository vanDauNguoiChoiRepository,
                          LichSuEloRepository lichSuEloRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.vanDauNguoiChoiRepository = vanDauNguoiChoiRepository;
        this.lichSuEloRepository = lichSuEloRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<NguoiDung> opt = nguoiDungRepository.findByTenDangNhap(username);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("user_not_found");
        NguoiDung nd = opt.get();

        List<VanDauNguoiChoi> matches = vanDauNguoiChoiRepository.findByNguoiDungOrderByIdDesc(nd);
        int total = matches.size();
        int wins = 0, losses = 0, draws = 0;
        for (VanDauNguoiChoi m : matches) {
            switch (m.getKetQua()) {
                case THANG -> wins++;
                case THUA -> losses++;
                default -> draws++;
            }
        }

        UserProfileDTO dto = new UserProfileDTO(nd.getTenDangNhap(), nd.getDiemElo(), total, wins, losses, draws);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/me/matches")
    public ResponseEntity<?> matches() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<NguoiDung> opt = nguoiDungRepository.findByTenDangNhap(username);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("user_not_found");
        NguoiDung nd = opt.get();

        List<VanDauNguoiChoi> entries = vanDauNguoiChoiRepository.findByNguoiDungOrderByIdDesc(nd);
        List<MatchDTO> out = new ArrayList<>();
        for (VanDauNguoiChoi e : entries) {
            VanDau vd = e.getVanDau();
            // find opponent
            List<VanDauNguoiChoi> others = vanDauNguoiChoiRepository.findByVanDauAndNguoiDungNot(vd, nd);
            String opponent = others.isEmpty() ? "unknown" : others.get(0).getNguoiDung().getTenDangNhap();
            MatchDTO m = new MatchDTO();
            m.setMatchId(vd.getId());
            m.setOpponent(opponent);
            m.setColor(e.getMauCo() == null ? null : (e.getMauCo().name().equals("TRANG") ? "WHITE" : "BLACK"));
            // result mapping
            switch (e.getKetQua()) {
                case THANG -> m.setResult("WIN");
                case THUA -> m.setResult("LOSS");
                default -> m.setResult("DRAW");
            }
            // elo before/after from lich_su_elo
            Optional<LichSuElo> le = lichSuEloRepository.findByNguoiDungAndVanDau(nd, vd);
            le.ifPresent(ls -> {
                m.setEloBefore(ls.getEloTruoc());
                m.setEloAfter(ls.getEloSau());
                m.setPlayedAt(ls.getThoiGian());
            });
            // fallback playedAt from VanDau
            if (m.getPlayedAt() == null) m.setPlayedAt(vd.getThoiGianKetThuc());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }
}


