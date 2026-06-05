package lsp.gunadarma.nilaimhs.controller;

import lsp.gunadarma.nilaimhs.entity.Guru;
import lsp.gunadarma.nilaimhs.entity.Siswa;
import lsp.gunadarma.nilaimhs.entity.Users;
import lsp.gunadarma.nilaimhs.repository.GuruRepository;
import lsp.gunadarma.nilaimhs.repository.SiswaRepository;
import lsp.gunadarma.nilaimhs.repository.UserRepository;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    //private static final Logger log = LoggerFactory.getLogger(AdminRestController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuruRepository guruRepository;

    @Autowired
    private SiswaRepository siswaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Struktur DTO (Data Transfer Object)
    

    // 1. CARI DATA (GET)
    @GetMapping("/users/{nomorInduk}")
    public ResponseEntity<?> cariData(@PathVariable("nomorInduk") String nomorInduk) {
        
        Optional<Users> userOpt = userRepository.findBynomorInduk(nomorInduk);
        
        if (userOpt.isEmpty()) {
            //log.info(nomorInduk);
            //System.out.println(nomorInduk);
            return ResponseEntity.status(404).body(Map.of("message", "Data tidak ditemukan!"));
        }

        Users user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("nomorInduk", user.getNomorInduk());
        response.put("role", user.getRole());
        // Catatan: Password sengaja tidak dikembalikan ke frontend demi keamanan

        // Tarik data spesifik berdasarkan role
        if (user.getRole().equals("GURU")) {
            guruRepository.findById(nomorInduk).ifPresent(guru -> {
                response.put("nama", guru.getNama());
                response.put("mataPelajaran", guru.getMata_pelajaran());
            });
        } else if (user.getRole().equals("SISWA")) {
            siswaRepository.findById(nomorInduk).ifPresent(siswa -> {
                response.put("nama", siswa.getNama());
                response.put("kelas", siswa.getKelas());
                response.put("nilaiTugas", siswa.getNilaiTugas());
                response.put("nilaiUts", siswa.getNilaiUts());
                response.put("nilaiUas", siswa.getNilaiUas());
            });
        }

        return ResponseEntity.ok(response);
    }

    // 2. DAFTAR USER BARU (POST)
    @PostMapping("/users")
    public ResponseEntity<?> mendaftarkanUser(@RequestBody ProtectedRestController.UserFormDTO formData) {
        // Cek apakah nomor induk sudah ada
        if (userRepository.findBynomorInduk(formData.nomorInduk).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nomor Induk sudah terdaftar!"));
        }

        // 1. Simpan ke tabel Users (Akun Login)
        Users newUser = new Users();
        newUser.setNomorInduk(formData.nomorInduk);
        newUser.setPassword(passwordEncoder.encode(formData.password));
        newUser.setRole(formData.role);
        userRepository.save(newUser);

        // 2. Simpan ke tabel spesifik (Guru/Siswa)
        if (formData.role.equals("GURU")) {
            Guru guru = new Guru();
            guru.setNip(formData.nomorInduk);
            guru.setNama(formData.nama);
            guru.setMata_pelajaran(formData.mataPelajaran);
            guruRepository.save(guru);
        } else if (formData.role.equals("SISWA")) {
            Siswa siswa = new Siswa();
            siswa.setNis(formData.nomorInduk);
            siswa.setNama(formData.nama);
            siswa.setKelas(formData.kelas);
            siswa.setNilaiTugas(formData.nilaiTugas);
            siswa.setNilaiUts(formData.nilaiUts);
            siswa.setNilaiUas(formData.nilaiUas);
            siswaRepository.save(siswa);
        }

        return ResponseEntity.ok(Map.of("message", "User " + formData.nomorInduk + " berhasil didaftarkan!"));
    }

    // 3. EDIT DATA (PUT)
    @PutMapping("/users/{nomorInduk}")
    public ResponseEntity<?> editData(@PathVariable("nomorInduk") String nomorInduk, @RequestBody ProtectedRestController.UserFormDTO formData) {
        Optional<Users> userOpt = userRepository.findBynomorInduk(nomorInduk);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Data tidak ditemukan!"));
        }

        Users user = userOpt.get();

        // Update password hanya jika form password diisi
        if (formData.password != null && !formData.password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(formData.password));
            userRepository.save(user);
        }

        // Update data spesifik (Kita asumsikan role tidak boleh diubah untuk menghindari error relasi logika)
        if (user.getRole().equals("GURU")) {
            Optional<Guru> guruOpt = guruRepository.findById(nomorInduk);
            if (guruOpt.isPresent()) {
                Guru guru = guruOpt.get();
                guru.setNama(formData.nama);
                guru.setMata_pelajaran(formData.mataPelajaran);
                guruRepository.save(guru);
            }
        } else if (user.getRole().equals("SISWA")) {
            Optional<Siswa> siswaOpt = siswaRepository.findById(nomorInduk);
            if (siswaOpt.isPresent()) {
                Siswa siswa = siswaOpt.get();
                siswa.setNama(formData.nama);
                siswa.setKelas(formData.kelas);
                siswa.setNilaiTugas(formData.nilaiTugas);
                siswa.setNilaiUts(formData.nilaiUts);
                siswa.setNilaiUas(formData.nilaiUas);
                siswaRepository.save(siswa);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Data " + nomorInduk + " berhasil diperbarui!"));
    }

    // 4. HAPUS DATA (DELETE)
    @DeleteMapping("/users/{nomorInduk}")
    public ResponseEntity<?> hapusData(@PathVariable("nomorInduk") String nomorInduk) {
        Optional<Users> userOpt = userRepository.findBynomorInduk(nomorInduk);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Data tidak ditemukan!"));
        }

        Users user = userOpt.get();

        // Hapus data spesifik terlebih dahulu
        if (user.getRole().equals("GURU")) {
            guruRepository.deleteById(nomorInduk);
        } else if (user.getRole().equals("SISWA")) {
            siswaRepository.deleteById(nomorInduk);
        }

        // Hapus akun login
        userRepository.delete(user);

        return ResponseEntity.ok(Map.of("message", "Data " + nomorInduk + " berhasil dihapus secara permanen!"));
    }

    // 5. AMBIL STATISTIK DASBOR (GET)
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        long totalGuru = guruRepository.count();
        long totalSiswa = siswaRepository.count();
        
        // Hitung nilai terproses (Siswa yang nilai Tugas, UTS, dan UAS-nya sudah terisi / tidak null)
        long nilaiTerproses = siswaRepository.findAll().stream()
                .filter(s -> s.getNilaiTugas() != null && s.getNilaiUts() != null && s.getNilaiUas() != null)
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalGuru", totalGuru);
        stats.put("totalSiswa", totalSiswa);
        stats.put("nilaiTerproses", nilaiTerproses);
        
        return ResponseEntity.ok(stats);
    }
}