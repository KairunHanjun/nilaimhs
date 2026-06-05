package lsp.gunadarma.nilaimhs.repository;

import lsp.gunadarma.nilaimhs.entity.Guru;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuruRepository extends JpaRepository<Guru, String>{
    // Fungsi bawaan seperti save(), findAll(), deleteById() sudah otomatis tersedia!
    // Kamu bisa tambah fungsi spesifik di sini jika perlu.
    Optional<Guru> findBynip(String nip);
}
