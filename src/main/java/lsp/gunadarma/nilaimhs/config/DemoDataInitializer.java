package lsp.gunadarma.nilaimhs.config;

import lsp.gunadarma.nilaimhs.entity.*;
import lsp.gunadarma.nilaimhs.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final GuruRepository teachers;
    private final SiswaRepository students;
    private final PasswordEncoder encoder;
    public DemoDataInitializer(UserRepository users, GuruRepository teachers, SiswaRepository students, PasswordEncoder encoder) {
        this.users = users; this.teachers = teachers; this.students = students; this.encoder = encoder;
    }
    @Override @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;
        account("admin", "ADMIN");
        teacher("19870001", "Ratna Prameswari", "Pemrograman Web");
        teacher("19870002", "Dimas Saputra", "Basis Data");
        teacher("19870003", "Maya Lestari", "Matematika");
        student("20260001", "Alya Pratama", "12-RPL A", 88, 84, 92);
        student("20260002", "Bima Saputra", "12-RPL A", 75, 70, 80);
        student("20260003", "Citra Lestari", "12-RPL A", 60, 55, 65);
        student("20260004", "Daffa Mahendra", "12-RPL B", 95, 90, 94);
        student("20260005", "Nadia Kirana", "12-RPL B", 80, null, null);
        student("20260006", "Raka Wijaya", "12-RPL B", 70, 70, 70);
        student("20260007", "Salsa Amalia", "12-RPL A", 0, 0, 0);
        student("20260008", "Fajar Ramadhan", "12-RPL B", null, null, null);
    }
    private void account(String id, String role) {
        Users user = new Users(); user.setNomorInduk(id); user.setRole(role);
        user.setPassword(encoder.encode("DemoNilai123!")); users.save(user);
    }
    private void teacher(String id, String name, String subject) {
        account(id, "GURU"); Guru guru = new Guru(); guru.setNip(id); guru.setNama(name);
        guru.setMata_pelajaran(subject); teachers.save(guru);
    }
    private void student(String id, String name, String group, Integer task, Integer uts, Integer uas) {
        account(id, "SISWA"); Siswa siswa = new Siswa(); siswa.setNis(id); siswa.setNama(name);
        siswa.setKelas(group); siswa.setNilaiTugas(task); siswa.setNilaiUts(uts); siswa.setNilaiUas(uas); students.save(siswa);
    }
}
