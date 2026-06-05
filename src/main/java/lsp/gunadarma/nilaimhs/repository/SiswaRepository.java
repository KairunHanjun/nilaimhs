package lsp.gunadarma.nilaimhs.repository;

import lsp.gunadarma.nilaimhs.entity.Siswa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiswaRepository extends JpaRepository<Siswa, String> {
    // Fungsi bawaan seperti save(), findAll(), deleteById() sudah otomatis tersedia!
    // Kamu bisa tambah fungsi spesifik di sini jika perlu.
    Optional<Siswa> findBynis(String nis);
}