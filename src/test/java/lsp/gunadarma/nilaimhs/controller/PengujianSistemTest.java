package lsp.gunadarma.nilaimhs.controller;

import tools.jackson.databind.ObjectMapper;
import lsp.gunadarma.nilaimhs.entity.Siswa;
import lsp.gunadarma.nilaimhs.repository.GuruRepository;
import lsp.gunadarma.nilaimhs.repository.SiswaRepository;
import lsp.gunadarma.nilaimhs.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

// Mengimpor fungsi pembantu MockMvc untuk GET, POST, dan CSRF
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PengujianSistemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Digunakan untuk mengubah Object DTO menjadi JSON string

    // Memalsukan (Mock) Repositori agar tidak mengubah isi database asli saat diuji
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private GuruRepository guruRepository;

    @MockitoBean
    private SiswaRepository siswaRepository;


    // =======================================================================
    // TC-01: PENGUJIAN AUTENTIKASI DAN REDIREKSI HALAMAN LOGIN
    // =======================================================================
    @Test
    @DisplayName("TC-01: Auth - Pengguna yang belum login mencoba akses halaman admin akan dialihkan (Redirect) ke halaman login")
    public void testBelumLoginAksesAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users/123"))
                // Berharap status 3xx (Redirect) ke halaman login atau 401 Unauthorized
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("TC-01: Auth - Pengguna yang sudah login akan ditendang ke dashboard jika mengetik URL /login")
    @WithMockUser(roles = "GURU") // Simulasi sudah login
    public void testSudahLoginAksesHalamanLogin() throws Exception {
        mockMvc.perform(get("/login"))
                // Berharap diarahkan kembali ke root "/" agar ditangkap oleh pengatur rute dashboard
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }


    // =======================================================================
    // TC-02: PENGUJIAN OTORISASI (KEAMANAN AKSES BERDASARKAN ROLE)
    // =======================================================================
    @Test
    @DisplayName("TC-02: Security - Siswa mencoba mengakses API milik Guru akan ditolak (403 Forbidden)")
    @WithMockUser(roles = "SISWA") // Simulasi login sebagai SISWA
    public void testKeamananAksesBedaRole() throws Exception {
        mockMvc.perform(get("/api/guru/siswa/12345")
                .contentType(MediaType.APPLICATION_JSON))
                // Validasi Keamanan: Spring Security harus langsung memblokir
                .andExpect(status().isForbidden());
    }


    // =======================================================================
    // TC-03: PENGUJIAN CRUD ADMIN (INSERT DATA & FETCH API POST)
    // =======================================================================
    @Test
    @DisplayName("TC-03: Admin CRUD - Admin berhasil menambahkan akun Guru baru")
    @WithMockUser(roles = "ADMIN") // Simulasi login sebagai ADMIN
    public void testAdminTambahGuruBaru() throws Exception {
        
        // 1. Siapkan data form yang dikirim dari Frontend
        ProtectedRestController.UserFormDTO formData = new ProtectedRestController.UserFormDTO();
        formData.nomorInduk = "19801122";
        formData.password = "rahasia123";
        formData.role = "GURU";
        formData.nama = "Budi Santoso";
        formData.mataPelajaran = "Matematika";

        // Simulasi bahwa NIP tersebut belum pernah terdaftar di database
        Mockito.when(userRepository.findBynomorInduk("19801122")).thenReturn(Optional.empty());

        // 2. Eksekusi request POST
        mockMvc.perform(post("/api/admin/users")
                .with(csrf()) // Menyuntikkan Token CSRF otomatis agar lolos proteksi keamanan
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formData))) // Ubah DTO ke JSON
                
                // 3. Validasi hasil dari Controller
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User 19801122 berhasil didaftarkan!"));
    }


    // =======================================================================
    // TC-04: PENGUJIAN GURU & KALKULASI NILAI AKHIR (LOGIKA BISNIS)
    // =======================================================================
    @Test
    @DisplayName("TC-04: Kalkulasi - Guru mencari siswa dan sistem mengalkulasi Nilai Akhir (30% + 30% + 40%)")
    @WithMockUser(roles = "GURU")
    public void testKalkulasiNilaiDanStatusKelulusan() throws Exception {
        
        Siswa mockSiswa = new Siswa();
        mockSiswa.setNis("12345");
        mockSiswa.setNama("Budi Santoso");
        mockSiswa.setKelas("12-RPL");
        // Rumus: (80*0.3) + (80*0.3) + (90*0.4) = 24 + 24 + 36 = 84.0
        mockSiswa.setNilaiTugas(80);
        mockSiswa.setNilaiUts(80);
        mockSiswa.setNilaiUas(90);

        Mockito.when(siswaRepository.findById("12345")).thenReturn(Optional.of(mockSiswa));

        mockMvc.perform(get("/api/protected/siswa/12345")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nis").value("12345"))
                .andExpect(jsonPath("$.nilaiAkhir").value(84.0)) // Validasi Matematika
                .andExpect(jsonPath("$.statusKelulusan").value("Lulus")); // Validasi Logika Kelulusan
    }


    // =======================================================================
    // TC-05: PENGUJIAN SISWA (PENCEGAHAN IDOR BERDASARKAN SESI)
    // =======================================================================
    @Test
    @DisplayName("TC-05: IDOR Protection - Siswa melihat laporan nilainya sendiri ditarik otomatis dari Sesi Login")
    @WithMockUser(username = "998877", roles = "SISWA") // Login dengan NIS: 998877
    public void testSiswaLihatLaporanku() throws Exception {
        
        Siswa mockSiswa = new Siswa();
        mockSiswa.setNis("998877");
        mockSiswa.setNama("Andi Wijaya");
        
        // Simulasi database menemukan siswa dengan NIS yang sama dengan Username Login
        Mockito.when(siswaRepository.findById("998877")).thenReturn(Optional.of(mockSiswa));

        // Perhatikan bahwa di URL TIDAK ADA parameter NIS (/api/siswa/laporanku)
        mockMvc.perform(get("/api/siswa/laporanku")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                // Validasi bahwa data yang dikembalikan benar-benar milik 998877
                .andExpect(jsonPath("$.nis").value("998877"))
                .andExpect(jsonPath("$.nama").value("Andi Wijaya"));
    }
}