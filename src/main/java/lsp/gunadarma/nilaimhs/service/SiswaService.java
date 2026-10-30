package lsp.gunadarma.nilaimhs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lsp.gunadarma.nilaimhs.entity.Siswa;
import lsp.gunadarma.nilaimhs.repository.SiswaRepository;

@Service
public class SiswaService {

    @Autowired
    private SiswaRepository siswaRepository;

    public java.util.Map<String, Object> laporan(Siswa siswa) {
        boolean complete = siswa.getNilaiTugas() != null && siswa.getNilaiUts() != null && siswa.getNilaiUas() != null;
        Double finalGrade = complete ? Math.round(hitungNilaiAkhir(siswa.getNilaiTugas(), siswa.getNilaiUts(), siswa.getNilaiUas()) * 100.0) / 100.0 : null;
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("nis", siswa.getNis()); result.put("nama", siswa.getNama()); result.put("kelas", siswa.getKelas());
        result.put("nilaiTugas", siswa.getNilaiTugas()); result.put("nilaiUTS", siswa.getNilaiUts()); result.put("nilaiUAS", siswa.getNilaiUas());
        result.put("nilaiAkhir", finalGrade); result.put("statusKelulusan", complete ? tentukanStatusKelulusan(finalGrade) : "");
        return result;
    }

    public Siswa saveStudentGrade(Siswa student) {
        // Validasi dilakukan sebelum menyimpan ke database
        if (student.getNilaiTugas() != null) validasiNilai(student.getNilaiTugas());
        if (student.getNilaiUts() != null) validasiNilai(student.getNilaiUts());
        if (student.getNilaiUas() != null) validasiNilai(student.getNilaiUas());

        return siswaRepository.save(student);
    }

    public void validasiNilai(int nilai) {
        if (nilai < 0 || nilai > 100) {
            throw new IllegalArgumentException("Error: Nilai harus berada dalam rentang 0 - 100");
        }
    }

    public double hitungNilaiAkhir(int tugas, int uts, int uas) {
        return (0.30 * tugas) + (0.30 * uts) + (0.40 * uas);
    }

    public String tentukanStatusKelulusan(double nilaiAkhir) {
        if (nilaiAkhir >= 70) {
            return "Lulus";
        } else {
            return "Tidak Lulus";
        }
    }

}
