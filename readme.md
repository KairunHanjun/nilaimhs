# 🎓 Sistem Pengolahan Nilai Akademik (Academic Grading System)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?logo=springboot)
![Java](https://img.shields.io/badge/Java-17%2B-blue.svg?logo=java)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template-green.svg?logo=thymeleaf)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-CDN-38B2AC.svg?logo=tailwind-css)

Aplikasi berbasis web untuk manajemen pengguna dan pengolahan nilai siswa yang dibangun menggunakan arsitektur **Model-View-Controller (MVC)** dengan **Spring Boot**. Proyek ini dirancang untuk memenuhi standar Uji Kompetensi Keahlian (LSP) dengan mengedepankan keamanan berlapis dan *User Experience* (UX) bergaya *Single Page Application* (SPA).

## ✨ Fitur Utama

Sistem ini memiliki pembagian hak akses (*Role-Based Access Control*) yang ketat untuk 3 jenis pengguna:

### 👨‍💻 1. Admin (Administrator)
* **Dashboard Statistik:** Pemantauan jumlah total Guru, Siswa, dan Nilai Terproses secara *real-time*.
* **Manajemen Pengguna (CRUD):** Mendaftarkan, mencari, mengedit, dan menghapus data pengguna (Admin/Guru/Siswa) dalam satu antarmuka SPA tanpa *reload* halaman (menggunakan Fetch API).

### 👨‍🏫 2. Guru
* **Kelola Nilai Siswa:** Pencarian data siswa berdasarkan NIS untuk melakukan *input* atau *update* Nilai Tugas, UTS, dan UAS.
* **Auto-Kalkulasi:** Sistem secara otomatis menghitung Nilai Akhir (30% Tugas + 30% UTS + 40% UAS) dan menentukan Status Kelulusan (Batas Minimal: 70).
* **Manajemen Profil:** Memperbarui data diri dan mengubah *password* secara mandiri.

### 👨‍🎓 3. Siswa
* **Laporan Akademik:** Melihat rincian nilai dan status kelulusan pribadi (Nilai ditarik otomatis dari Sesi Login / *Authentication Context*).
* **Manajemen Profil:** Memperbarui nama, kelas, dan *password* akun pribadi.

---

## 🛡️ Sorotan Keamanan (Security Highlights)

Proyek ini tidak hanya fungsional, tetapi juga mengimplementasikan standar keamanan web industri (*Best Practices*):

1. **Proteksi IDOR (*Insecure Direct Object Reference*):** Siswa tidak dapat memanipulasi URL (misal: mengganti NIS) untuk melihat nilai siswa lain. Sistem mengikat akses data mutlak pada Sesi Login saat itu.
2. **Proteksi CSRF (*Cross-Site Request Forgery*):** Seluruh *endpoint* REST API yang bersifat mengubah data (POST, PUT, DELETE) dilindungi oleh Token CSRF dinamis.
3. **Kriptografi Kredensial:** Kata sandi (*password*) dienkripsi secara satu arah (*one-way hash*) menggunakan algoritma **BCrypt**.
4. **Pemisahan Endpoint API:** Akses ke manipulasi data dilindungi menggunakan fungsi `.hasRole()` dari Spring Security (Siswa tidak bisa mengakses API milik Guru/Admin meskipun mengetahui URL-nya).

---

## 🛠️ Teknologi yang Digunakan

**Backend:**
* Java 17 / 21
* Spring Boot (Web, Data JPA)
* Spring Security (Autentikasi & Otorisasi)
* Database MySQL / H2
* JUnit 5 & MockMvc (Automated Testing)

**Frontend:**
* HTML5 & Vanilla JavaScript (ES6+ Fetch API)
* Thymeleaf (Server-side Template Engine)
* Tailwind CSS (Styling via CDN)

---

## 🚀 Cara Menjalankan Proyek (Getting Started)

### Prasyarat
* Java Development Kit (JDK) 17 atau lebih baru
* Maven / Gradle
* MySQL Server (Atau sesuaikan konfigurasi database di `application.properties`)

### Instalasi
1. Clone repositori ini:
   ```
   bash
   git clone https://github.com/username-kamu/nama-repo-kamu.git
   cd nama-repo-kamu
   ```
2. Buka file `src/main/resources/application.properties` (atau `application.yml`) dan sesuaikan konfigurasi koneksi database Anda:
   ```
   properties
   spring.datasource.url=jdbc:mysql://localhost:3306/db_nilaimhs
   spring.datasource.username=root
   spring.datasource.password=password_anda
   spring.jpa.hibernate.ddl-auto=update
   ```
3. Jalankan aplikasi:
   ```
   bash
   # Jika menggunakan Gradle
   ./gradlew bootRun
   
   # Jika menggunakan Maven
   ./mvnw spring-boot:run
   ```
4. Buka browser dan akses aplikasi di: **`http://localhost:8080`**

---

## 🧪 Pengujian (Testing)
Proyek ini dilengkapi dengan *Unit Testing* dan *Integration Testing* menggunakan JUnit 5. Pengujian mencakup:
* Simulasi Login & Keamanan Peran (Role Security).
* Logika Kalkulasi Nilai Akhir.
* Pengujian REST API (MockMvc) beserta injeksi CSRF Token.

Untuk menjalankan *test*:
```bash
./gradlew test
# atau
./mvnw test
```

---

## 📸 Tangkapan Layar (Screenshots)

| Halaman Login | Dashboard Admin (SPA) |
| :---: | :---: |
| <img src="[https://via.placeholder.com/400x250?text=Screenshot+Login](https://via.placeholder.com/400x250?text=Screenshot+Login)" width="400"> | <img src="[https://via.placeholder.com/400x250?text=Screenshot+Admin](https://via.placeholder.com/400x250?text=Screenshot+Admin)" width="400"> |

| Dashboard Guru (Input Nilai) | Dashboard Siswa (Laporan) |
| :---: | :---: |
| <img src="[https://via.placeholder.com/400x250?text=Screenshot+Guru](https://via.placeholder.com/400x250?text=Screenshot+Guru)" width="400"> | <img src="[https://via.placeholder.com/400x250?text=Screenshot+Siswa](https://via.placeholder.com/400x250?text=Screenshot+Siswa)" width="400"> |

---

## 📄 Lisensi
Dibuat dan dikembangkan untuk keperluan Uji Kompetensi LSP.