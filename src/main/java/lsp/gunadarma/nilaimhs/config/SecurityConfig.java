package lsp.gunadarma.nilaimhs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // Akses ketat berdasarkan Role
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN") 
                .requestMatchers("/guru/**", "/api/guru/**").hasRole("GURU")
                .requestMatchers("/siswa/**", "/api/siswa/**").hasRole("SISWA")
                // Akses gabungan (Misal: pencarian siswa bisa dilakukan oleh Admin, Guru, dan Siswa itu sendiri)
                .requestMatchers("/api/protected/**").hasAnyRole("ADMIN", "GURU")
                .requestMatchers("/api/protected/profil").hasAllRoles("GURU", "SISWA")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // diarahkan ke root untuk pengecekan peran (role)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Digunakan untuk mencocokkan hash password
    }
}