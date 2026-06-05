package lsp.gunadarma.nilaimhs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nomor_induk", unique = true, nullable = false)
    private String nomorInduk;

    @Column(nullable = false)
    private String password;

    // Role bisa diisi: ADMIN, GURU, SISWA
    @Column(nullable = false)
    private String role;
}
