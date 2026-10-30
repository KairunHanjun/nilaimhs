package lsp.gunadarma.nilaimhs.controller;

import lsp.gunadarma.nilaimhs.entity.*;
import lsp.gunadarma.nilaimhs.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DemoWorkflowTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired SiswaRepository students;
    @Autowired GuruRepository teachers;
    @Autowired PasswordEncoder encoder;

    @BeforeEach void setup() {
        account("admin", "ADMIN"); account("teacher", "GURU"); account("student", "SISWA"); account("incomplete", "SISWA");
        Guru teacher = new Guru(); teacher.setNip("teacher"); teacher.setNama("Guru Demo"); teacher.setMata_pelajaran("Basis Data"); teachers.save(teacher);
        Siswa student = new Siswa(); student.setNis("student"); student.setNama("Siswa Demo"); student.setKelas("12-RPL"); student.setNilaiTugas(0); student.setNilaiUts(0); student.setNilaiUas(0); students.save(student);
        Siswa incomplete = new Siswa(); incomplete.setNis("incomplete"); incomplete.setNama("Siswa Belum Dinilai"); incomplete.setKelas("12-RPL"); students.save(incomplete);
    }
    void account(String id, String role) { Users user=new Users(); user.setNomorInduk(id); user.setRole(role); user.setPassword(encoder.encode("DemoNilai123!")); users.save(user); }

    @Test void realLoginWorks() throws Exception {
        mvc.perform(formLogin().user("teacher").password("DemoNilai123!"))
            .andExpect(status().is3xxRedirection()).andExpect(authenticated().withUsername("teacher"));
    }
    @Test void loginPageRenders() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Masuk ke akun")));
    }
    @Test @WithMockUser(username="admin",roles="ADMIN") void adminPageAndStatistics() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk()).andExpect(content().string(containsString("Kelola pengguna")));
        mvc.perform(get("/api/admin/stats")).andExpect(jsonPath("$.totalSiswa").value(2)).andExpect(jsonPath("$.nilaiTerproses").value(1));
        mvc.perform(get("/api/admin/users")).andExpect(status().isOk()).andExpect(content().string(not(containsString("password"))));
    }
    @Test @WithMockUser(username="teacher",roles="GURU") void teacherPageRendersWithProfile() throws Exception {
        mvc.perform(get("/guru")).andExpect(status().isOk()).andExpect(content().string(containsString("Guru Demo")));
    }
    @Test @WithMockUser(username="student",roles="SISWA") void studentPageRenders() throws Exception {
        mvc.perform(get("/siswa")).andExpect(status().isOk()).andExpect(content().string(containsString("Laporan akademik saya")));
    }
    @Test @WithMockUser(username="student",roles="SISWA") void studentCanEditOwnProfileButNotGradesOrIdentity() throws Exception {
        mvc.perform(put("/api/protected/profil").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nama\":\"Nama Baru\",\"kelas\":\"12-RPL B\",\"nomorInduk\":\"incomplete\",\"nilaiTugas\":100}"))
            .andExpect(status().isOk());
        assertThat(students.findById("student").orElseThrow().getNama()).isEqualTo("Nama Baru");
        assertThat(students.findById("student").orElseThrow().getNilaiTugas()).isZero();
        assertThat(students.findById("incomplete").orElseThrow().getNama()).isEqualTo("Siswa Belum Dinilai");
    }
    @Test @WithMockUser(username="student",roles="SISWA") void studentCannotReadOthersOrRoster() throws Exception {
        mvc.perform(get("/api/protected/siswa/incomplete")).andExpect(status().isForbidden());
        mvc.perform(get("/api/protected/siswa")).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(username="student",roles="SISWA") void zeroGradesAreCompleteAndFail() throws Exception {
        mvc.perform(get("/api/siswa/laporanku")).andExpect(jsonPath("$.nis").value("student"))
            .andExpect(jsonPath("$.nilaiTugas").value(0)).andExpect(jsonPath("$.nilaiAkhir").value(0.0))
            .andExpect(jsonPath("$.statusKelulusan").value("Tidak Lulus"));
    }
    @Test @WithMockUser(username="incomplete",roles="SISWA") void missingGradesHaveNoFinalScore() throws Exception {
        mvc.perform(get("/api/siswa/laporanku")).andExpect(jsonPath("$.nilaiAkhir").isEmpty())
            .andExpect(jsonPath("$.statusKelulusan").value(""));
    }
    @Test @WithMockUser(username="teacher",roles="GURU") void validGradesPersistAndRespectBoundary() throws Exception {
        mvc.perform(put("/api/guru/siswa/student").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nilaiTugas\":70,\"nilaiUts\":70,\"nilaiUas\":70,\"nama\":\"Tidak Diubah\"}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/protected/siswa/student")).andExpect(jsonPath("$.nilaiAkhir").value(70.0))
            .andExpect(jsonPath("$.statusKelulusan").value("Lulus")).andExpect(jsonPath("$.nama").value("Siswa Demo"));
    }
    @Test @WithMockUser(username="teacher",roles="GURU") void invalidGradesDoNotPersist() throws Exception {
        mvc.perform(put("/api/guru/siswa/student").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nilaiTugas\":101,\"nilaiUts\":90}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        assertThat(students.findById("student").orElseThrow().getNilaiUts()).isZero();
    }
    @Test @WithMockUser(username="teacher",roles="GURU") void fractionalGradesAreRejected() throws Exception {
        mvc.perform(put("/api/guru/siswa/student").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nilaiTugas\":90.5}"))
            .andExpect(status().isBadRequest());
        assertThat(students.findById("student").orElseThrow().getNilaiTugas()).isZero();
    }
    @Test @WithMockUser(username="teacher",roles="GURU") void missingAndNonStudentEditsReturnErrors() throws Exception {
        mvc.perform(put("/api/guru/siswa/missing").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isNotFound());
        mvc.perform(put("/api/guru/siswa/admin").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }
    @Test @WithMockUser(username="admin",roles="ADMIN") void csrfIsRequiredForDeletion() throws Exception {
        mvc.perform(delete("/api/admin/users/student")).andExpect(status().isForbidden());
        assertThat(users.findBynomorInduk("student")).isPresent();
    }
    @Test @WithMockUser(username="admin",roles="ADMIN") void deletionRemovesBothAccountAndProfile() throws Exception {
        mvc.perform(delete("/api/admin/users/student").with(csrf())).andExpect(status().isOk());
        assertThat(users.findBynomorInduk("student")).isEmpty(); assertThat(students.findById("student")).isEmpty();
    }
    @Test @WithMockUser(username="admin",roles="ADMIN") void adminCannotDeleteSelf() throws Exception {
        mvc.perform(delete("/api/admin/users/admin").with(csrf())).andExpect(status().isBadRequest());
        assertThat(users.findBynomorInduk("admin")).isPresent();
    }
    @Test @WithMockUser(username="admin",roles="ADMIN") void createUserPersistsHashedPasswordAndProfile() throws Exception {
        mvc.perform(post("/api/admin/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nomorInduk\":\"newstudent\",\"role\":\"SISWA\",\"password\":\"Password123!\",\"nama\":\"Siswa Baru\",\"kelas\":\"12-RPL\",\"nilaiTugas\":0}"))
            .andExpect(status().isOk());
        assertThat(encoder.matches("Password123!",users.findBynomorInduk("newstudent").orElseThrow().getPassword())).isTrue();
        assertThat(students.findById("newstudent").orElseThrow().getNilaiTugas()).isZero();
    }
    @Test @WithMockUser(username="admin",roles="ADMIN") void invalidCreateCannotLeaveOrphanedAccount() throws Exception {
        mvc.perform(post("/api/admin/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nomorInduk\":\"invalid\",\"role\":\"SISWA\",\"password\":\"Password123!\",\"nama\":\"Siswa\",\"kelas\":\"12-RPL\",\"nilaiTugas\":-1}"))
            .andExpect(status().isBadRequest());
        assertThat(users.findBynomorInduk("invalid")).isEmpty();
        mvc.perform(post("/api/admin/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nomorInduk\":\"invalid\",\"role\":\"UNKNOWN\",\"password\":\"Password123!\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test @WithMockUser(username="admin",roles="ADMIN") void editsRejectRoleChangesAndInvalidGrades() throws Exception {
        mvc.perform(put("/api/admin/users/student").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nama\":\"Siswa\",\"kelas\":\"12-RPL\",\"role\":\"ADMIN\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/users/student").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nama\":\"Siswa\",\"kelas\":\"12-RPL\",\"nilaiUas\":101}"))
            .andExpect(status().isBadRequest());
    }
}
