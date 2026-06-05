package lsp.gunadarma.nilaimhs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "siswa")
@Data
@NoArgsConstructor
public class Siswa {
    @Id
    @Column(name = "nis", unique = true, nullable = false, length = 20)
    private String nis;

    @Column(nullable = false)
    private String nama;

    @Column(name = "kelas", nullable = false)
    private String kelas;

    // Nilai digabung ke tabel siswa sesuai ketentuan dokumen
    @Column(name = "nilai_tugas")
    private Integer nilaiTugas;

    @Column(name = "nilai_uts")
    private Integer nilaiUts;

    @Column(name = "nilai_uas")
    private Integer nilaiUas;
}