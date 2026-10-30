package lsp.gunadarma.nilaimhs.service;

import lsp.gunadarma.nilaimhs.controller.ProtectedRestController.UserFormDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Set;

public final class FormValidation {
    private FormValidation() {}
    public static void require(boolean valid, String message) {
        if (!valid) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    public static void grades(UserFormDTO form) {
        for (Integer value : new Integer[]{form.nilaiTugas, form.nilaiUts, form.nilaiUas}) {
            require(value == null || value >= 0 && value <= 100, "Nilai harus berada dalam rentang 0 - 100.");
        }
    }
    public static void password(String value, boolean required) {
        if (!required && (value == null || value.isBlank())) return;
        require(value != null && value.length() >= 8 && value.length() <= 72
            && value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
            "Password harus 8 - 72 karakter dan maksimal 72 byte.");
    }
    public static void text(String value, String field, boolean required) {
        require(!required || value != null && !value.isBlank(), field + " wajib diisi.");
        require(value == null || value.length() <= 100, field + " maksimal 100 karakter.");
    }
    public static void user(UserFormDTO form, String role, boolean create) {
        grades(form);
        password(form.password, create);
        if (create) {
            require(form.nomorInduk != null && form.nomorInduk.matches("[A-Za-z0-9_-]{3,20}"),
                "Nomor induk harus 3 - 20 karakter: huruf, angka, garis bawah, atau tanda hubung.");
            require(role != null && Set.of("ADMIN", "GURU", "SISWA").contains(role), "Role tidak valid.");
        }
        if (!"ADMIN".equals(role)) text(form.nama, "Nama", true);
        if ("GURU".equals(role)) text(form.mataPelajaran, "Mata pelajaran", true);
        if ("SISWA".equals(role)) text(form.kelas, "Kelas", true);
    }
}
