package lsp.gunadarma.nilaimhs.controller;

import lsp.gunadarma.nilaimhs.entity.Guru;
import lsp.gunadarma.nilaimhs.entity.Siswa;
import lsp.gunadarma.nilaimhs.entity.Users;
import lsp.gunadarma.nilaimhs.repository.GuruRepository;
import lsp.gunadarma.nilaimhs.repository.SiswaRepository;
import lsp.gunadarma.nilaimhs.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/protected")
public class ProtectedRestController {

    public static class UserFormDTO {
        public String nomorInduk;
        public String password;
        public String role;
        public String nama;
        public String mataPelajaran; 
        public String kelas;         
        public Integer nilaiTugas;
        public Integer nilaiUts;
        public Integer nilaiUas;
    }

    @Autowired
    private SiswaRepository siswaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuruRepository guruRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/siswa/{nis}")
    public ResponseEntity<?> cariSiswa(@PathVariable("nis") String nis) {
        Optional<Siswa> siswaOpt = siswaRepository.findById(nis); // Sesuaikan dengan nama method di repositorimu
        if (siswaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Data tidak ditemukan!"));
        }
        
        Siswa newSiswa = siswaOpt.get();
        double nilaiAkhir = 0.0;
        String statusKelulusan = "";

        if (newSiswa.getNilaiTugas() != null && newSiswa.getNilaiUts() != null && newSiswa.getNilaiUas() != null) {
            nilaiAkhir = (0.30 * newSiswa.getNilaiTugas()) + (0.30 * newSiswa.getNilaiUts()) + (0.40 * newSiswa.getNilaiUas());
            if (nilaiAkhir >= 70) {
                statusKelulusan = "Lulus";
            } else {
                statusKelulusan = "Tidak Lulus";
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("nis", newSiswa.getNis());
        response.put("nama", newSiswa.getNama());
        response.put("kelas", newSiswa.getKelas());
        response.put("nilaiTugas", newSiswa.getNilaiTugas());
        response.put("nilaiUTS", newSiswa.getNilaiUts());
        response.put("nilaiUAS", newSiswa.getNilaiUas());
        response.put("nilaiAkhir", nilaiAkhir);
        response.put("statusKelulusan", statusKelulusan);

        return ResponseEntity.ok(response);
    }

    // 2. EDIT PROFIL SENDIRI (Otomatis mendeteksi Guru atau Siswa dari sesi Login)
    @PutMapping("/profil")
    public ResponseEntity<?> editProfilSendiri(@RequestBody UserFormDTO formData, Authentication authentication) {
        
        // MENGAMBIL IDENTITAS SAH DARI SESI LOGIN (Bukan dari URL)
        String nomorIndukLogin = authentication.getName(); 
        
        Optional<Users> userOpt = userRepository.findBynomorInduk(nomorIndukLogin);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Akun Anda tidak ditemukan di sistem!"));
        }

        Users user = userOpt.get();

        // A. Update Password (Berlaku untuk Guru dan Siswa)
        if (formData.password != null && !formData.password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(formData.password));
            userRepository.save(user);
        }

        // B. Update Data Spesifik berdasarkan Role
        if (user.getRole().equalsIgnoreCase("GURU")) {
            Optional<Guru> guruOpt = guruRepository.findById(nomorIndukLogin);
            if (guruOpt.isPresent()) {
                Guru guru = guruOpt.get();
                // Update nama jika diisi
                if (formData.nama != null && !formData.nama.trim().isEmpty()) {
                    guru.setNama(formData.nama);
                }
                // Update mata pelajaran jika diisi
                if (formData.mataPelajaran != null && !formData.mataPelajaran.trim().isEmpty()) {
                    guru.setMata_pelajaran(formData.mataPelajaran);
                }
                guruRepository.save(guru);
            }
        } 
        else if (user.getRole().equalsIgnoreCase("SISWA")) {
            Optional<Siswa> siswaOpt = siswaRepository.findById(nomorIndukLogin);
            if (siswaOpt.isPresent()) {
                Siswa siswa = siswaOpt.get();
                // Update nama jika diisi
                if (formData.nama != null && !formData.nama.trim().isEmpty()) {
                    siswa.setNama(formData.nama);
                }
                // Update kelas jika diisi
                if (formData.kelas != null && !formData.kelas.trim().isEmpty()) {
                    siswa.setKelas(formData.kelas);
                }
                // Catatan Keamanan: Nilai (Tugas, UTS, UAS) TIDAK DISERTAKAN di sini 
                // agar Siswa tidak bisa meretas dan mengubah nilainya sendiri.
                
                siswaRepository.save(siswa);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Profil Anda berhasil diperbarui!"));
    }
}