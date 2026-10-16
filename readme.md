# Nilai Akademik

**Sistem pengolahan nilai dengan akses Admin, Guru, dan Siswa.**

[English](README.en.md) · [Kode sumber](https://github.com/KairunHanjun/nilaimhs) · [Apache 2.0](LICENSE)

Nilai Akademik menghubungkan administrasi pengguna, input komponen nilai, dan laporan siswa dalam satu aplikasi web. Proyek pribadi ini dikembangkan untuk mempelajari alur data akademik, autentikasi, dan pembagian akses menggunakan ekosistem Java.

## Fitur utama

| Peran | Alur kerja |
| --- | --- |
| Admin | Mengelola pengguna dan profil, memantau statistik, serta melihat rekap nilai. |
| Guru | Mengisi nilai tugas, UTS, dan UAS serta memperbarui profil. |
| Siswa | Melihat laporan pribadi, memperbarui profil, dan mencetak laporan. |

Pada versi demo portofolio, nilai akhir dihitung dengan bobot **30% tugas + 30% UTS + 40% UAS**, dengan batas lulus **70**. Nilai yang belum lengkap dibedakan dari nilai nol.

## Teknologi

| Bagian | Teknologi |
| --- | --- |
| Bahasa & framework | Java 25, Spring Boot 4.0.6 |
| Antarmuka | Thymeleaf, HTML, CSS, JavaScript |
| Akses & data | Spring Security, Spring Data JPA, PostgreSQL |
| Build & pengujian | Gradle Wrapper, JUnit, Spring testing |
| Demo portofolio lokal | H2 di memori, data fiktif |

## Menjalankan source repository

Siapkan **JDK 25**, PostgreSQL, dan konfigurasi datasource sesuai lingkungan Anda. Dependensi diunduh pada build pertama. Gunakan konfigurasi lokal atau environment variable; jangan menyimpan kredensial database dalam README.

```sh
git clone https://github.com/KairunHanjun/nilaimhs.git
cd nilaimhs
./gradlew bootRun
```

Pada Windows, gunakan `gradlew.bat` sebagai pengganti `./gradlew`. Konfigurasi database dan dependensi versi repository perlu diperiksa sebelum menjalankan; panduan ini bukan klaim bahwa source awal siap produksi.

## Demo portofolio lokal

> Bagian ini mendokumentasikan versi demo yang telah disiapkan di workspace portofolio. Commit pembaruan README/LICENSE ini tidak menyertakan perubahan source demo yang masih lokal. Periksa keberadaan `start-demo.ps1` dan profil `application-demo.yml` sebelum memakai perintah berikut.

```powershell
.\start-demo.ps1
# Port alternatif:
.\start-demo.ps1 -Port 8081
```

Dengan source demo yang sesuai, alternatif lintas platform:

```sh
./gradlew bootRun --args='--spring.profiles.active=demo'
```

Buka **http://127.0.0.1:8080**. Hentikan dengan `Ctrl+C`. Launcher memilih JDK portable dari `../.portfolio-tools` bila tersedia; selain itu gunakan `JAVA_HOME` yang menunjuk JDK 25.

| Peran | Nomor induk | Kata sandi demo |
| --- | --- | --- |
| Admin | `admin` | `DemoNilai123!` |
| Guru | `19870001` | `DemoNilai123!` |
| Siswa | `20260001` | `DemoNilai123!` |

H2 memuat **1 admin, 3 guru, dan 8 siswa fiktif**. Data kembali ke kondisi awal setelah restart. Akun tersebut khusus demo lokal dan bukan akun produksi.

## Build dan pengujian

```sh
./gradlew test bootJar
```

- Laporan tes: `build/reports/tests/test/index.html`.
- Hasil build: `build/libs/`; nama JAR mengikuti versi pada konfigurasi Gradle.
- Pemeriksaan demo lokal mencakup login tiga peran, CSRF, pembatasan akses, konsistensi akun/profil, validasi nilai, serta nilai nol dan belum lengkap. Hasil tersebut mengacu pada source demo lokal, bukan hasil CI otomatis repository ini.

## Struktur ringkas

```text
src/main/java/         Controller, service, repository, dan konfigurasi
src/main/resources/    Template, aset, dan konfigurasi aplikasi
src/test/              Pengujian aplikasi
gradle/                Gradle Wrapper
```

## Catatan pengembangan

Bobot penilaian masih tetap. Konfigurasi kurikulum, periode akademik, audit perubahan nilai, dan deployment produksi merupakan ruang pengembangan berikutnya. Data dan screenshot portofolio tidak menunjukkan penggunaan oleh sekolah nyata. Screenshot demo berada di workspace terpisah `../portfolio-materials/screenshots/nilaimhs`, bukan bagian dari clone repository ini.

Proyek awal dikembangkan mandiri oleh **Guntur Ahmad Lion Putranto**. Penyempurnaan demo dan dokumentasi portofolio dibantu Codex.

## Lisensi

Kode proyek dilisensikan dengan **Apache License 2.0**. Teks lengkap tersedia pada [LICENSE](LICENSE). Dependensi dan aset pihak ketiga tetap mengikuti lisensinya masing-masing.
