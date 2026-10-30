package lsp.gunadarma.nilaimhs.controller;

import lsp.gunadarma.nilaimhs.entity.Siswa;
import lsp.gunadarma.nilaimhs.repository.SiswaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/siswa")
public class SiswaRestController {

    @Autowired
    private SiswaRepository siswaRepository;

    @Autowired
    private lsp.gunadarma.nilaimhs.service.SiswaService siswaService;

    // AMBIL LAPORAN NILAI SENDIRI BERDASARKAN SESI LOGIN
    @GetMapping("/laporanku")
    public ResponseEntity<?> lihatLaporanku(Authentication authentication) {
        
        // Menarik NIS sah dari user yang sedang login
        String nisLogin = authentication.getName();
        
        Optional<Siswa> siswaOpt = siswaRepository.findById(nisLogin);
        
        if (siswaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Data Anda tidak ditemukan di sistem!"));
        }

        return ResponseEntity.ok(siswaService.laporan(siswaOpt.get()));
    }
}