package lsp.gunadarma.nilaimhs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guru")
@Data
@NoArgsConstructor
public class Guru {
    @Id
    @Column(name = "nip", unique = true, nullable = false, length = 20)
    private String nip;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false)
    private String mata_pelajaran;
}
