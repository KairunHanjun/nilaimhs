package lsp.gunadarma.nilaimhs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lsp.gunadarma.nilaimhs.repository.GuruRepository;
import lsp.gunadarma.nilaimhs.repository.SiswaRepository;

@Controller
public class MainController {

    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        // Cek apakah pengguna sudah terautentikasi dan BUKAN anonymous
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // Jika sudah login, kembalikan ke root ("/") agar diarahkan otomatis ke dashboard sesuai role
            return "redirect:/"; 
        }
        
        // Jika belum login, tampilkan halaman login
        return "login";
    }

    // Halaman Utama (Akar/Root) berfungsi sebagai pembagi jalur berdasarkan Role
    @GetMapping("/")
    public String rootRedirect(Authentication authentication) {
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    return "redirect:/admin";
                }else if (authority.getAuthority().equals("ROLE_GURU")){
                    return "redirect:/guru";
                }else if (authority.getAuthority().equals("ROLE_SISWA")){
                    return "redirect:/siswa";
                }
                // Anda bisa menambahkan pengalihan untuk ROLE_GURU atau ROLE_SISWA di sini nanti
            }
        }
        return "redirect:/login";
    }

    // Halaman Dashboard Utama Admin
    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin"; // Memanggil src/main/resources/templates/admin.html
    }

    @Autowired
    private GuruRepository guruRepository; // Jangan lupa inject repository-nya

    @GetMapping("/guru")
    public String guruDashboard(Authentication authentication, Model model) {
        // Ambil NIP dari sesi login saat ini (karena username login adalah nomor induk)
        String nip = authentication.getName();
        
        // Cari data profil guru dan lempar ke Thymeleaf
        guruRepository.findById(nip).ifPresent(guru -> {
            model.addAttribute("guru", guru);
        });
        
        return "guru";
    }
    @Autowired
    private SiswaRepository siswaRepository; // Inject ini di bagian atas MainController

    @GetMapping("/siswa")
    public String siswaDashboard(Authentication authentication, Model model) {
        String nis = authentication.getName();
        
        // Cari profil siswa dan kirim ke Thymeleaf
        siswaRepository.findById(nis).ifPresent(siswa -> {
            model.addAttribute("siswa", siswa);
        });
        
        return "siswa"; // Akan memanggil src/main/resources/templates/siswa.html
    }
}