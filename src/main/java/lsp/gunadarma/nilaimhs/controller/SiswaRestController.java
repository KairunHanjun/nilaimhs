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

    // AMBIL LAPORAN NILAI SENDIRI BERDASARKAN SESI LOGIN
    @GetMapping("/laporanku")
    public ResponseEntity<?> lihatLaporanku(Authentication authentication) {
        
        // Menarik NIS sah dari user yang sedang login
        String nisLogin = authentication.getName();
        
        Optional<Siswa> siswaOpt = siswaRepository.findById(nisLogin);
        
        if (siswaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Data Anda tidak ditemukan di sistem!"));
        }

        Siswa siswa = siswaOpt.get();
        double nilaiAkhir = 0.0;
        String statusKelulusan = "";

        // Logika kalkulasi (Hanya hitung jika semua nilai sudah diinput oleh guru)
        if (siswa.getNilaiTugas() != null && siswa.getNilaiUts() != null && siswa.getNilaiUas() != null) {
            nilaiAkhir = (0.30 * siswa.getNilaiTugas()) + (0.30 * siswa.getNilaiUts()) + (0.40 * siswa.getNilaiUas());
            if (nilaiAkhir >= 70) {
                statusKelulusan = "Lulus";
            } else {
                statusKelulusan = "Tidak Lulus";
            }
        }

        // Bungkus data ke dalam JSON
        Map<String, Object> response = new HashMap<>();
        response.put("nis", siswa.getNis());
        response.put("nama", siswa.getNama());
        response.put("kelas", siswa.getKelas());
        response.put("nilaiTugas", siswa.getNilaiTugas());
        response.put("nilaiUTS", siswa.getNilaiUts());
        response.put("nilaiUAS", siswa.getNilaiUas());
        response.put("nilaiAkhir", nilaiAkhir);
        response.put("statusKelulusan", statusKelulusan);

        return ResponseEntity.ok(response);
    }
}