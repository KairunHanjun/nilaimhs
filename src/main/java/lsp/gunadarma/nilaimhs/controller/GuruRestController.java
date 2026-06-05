package lsp.gunadarma.nilaimhs.controller;

import lsp.gunadarma.nilaimhs.entity.Siswa;
import lsp.gunadarma.nilaimhs.entity.Users;
import lsp.gunadarma.nilaimhs.repository.SiswaRepository;
import lsp.gunadarma.nilaimhs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/guru")
public class GuruRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiswaRepository siswaRepository;

    // Gunakan class DTO yang sama atau import dari AdminRestController.UserFormDTO
    @PutMapping("/siswa/{nis}")
    public ResponseEntity<?> editDataSiswa(@PathVariable("nis") String nis, @RequestBody ProtectedRestController.UserFormDTO formData) {
        Optional<Users> userOpt = userRepository.findBynomorInduk(nis);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Data tidak ditemukan!"));
        }

        // VALIDASI KETAT: Pastikan yang diedit HANYA role SISWA
        if (!userOpt.get().getRole().equalsIgnoreCase("SISWA")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Akses ditolak! Anda hanya dapat mengubah data Siswa."));
        }

        Optional<Siswa> siswaOpt = siswaRepository.findById(nis);
        if (siswaOpt.isPresent()) {
            Siswa siswa = siswaOpt.get();
            // Guru hanya mengubah nilai (nama dan kelas dibiarkan utuh untuk mencegah manipulasi)
            if (formData.nilaiTugas != null) siswa.setNilaiTugas(formData.nilaiTugas);
            if (formData.nilaiUts != null) siswa.setNilaiUts(formData.nilaiUts);
            if (formData.nilaiUas != null) siswa.setNilaiUas(formData.nilaiUas);
            
            siswaRepository.save(siswa);
            return ResponseEntity.ok(Map.of("message", "Nilai Siswa " + nis + " berhasil diperbarui!"));
        }

        return ResponseEntity.status(500).body(Map.of("message", "Gagal memperbarui data."));
    }

    // @DeleteMapping("/siswa/{nis}")
    // public ResponseEntity<?> hapusDataSiswa(@PathVariable("nis") String nis) {
    //     Optional<Users> userOpt = userRepository.findBynomorInduk(nis);
        
    //     if (userOpt.isEmpty()) {
    //         return ResponseEntity.status(404).body(Map.of("message", "Data tidak ditemukan!"));
    //     }

    //     // VALIDASI KETAT: Pastikan yang dihapus HANYA role SISWA
    //     if (!userOpt.get().getRole().equalsIgnoreCase("SISWA")) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //                 .body(Map.of("message", "Akses ditolak! Anda hanya dapat menghapus data Siswa."));
    //     }

    //     // Proses hapus dari tabel Siswa, lalu tabel Users
    //     siswaRepository.deleteById(nis);
    //     userRepository.delete(userOpt.get());

    //     return ResponseEntity.ok(Map.of("message", "Data Siswa " + nis + " berhasil dihapus!"));
    // }
}