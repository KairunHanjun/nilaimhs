package lsp.gunadarma.nilaimhs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lsp.gunadarma.nilaimhs.entity.Users;
import lsp.gunadarma.nilaimhs.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String nomorInduk) throws UsernameNotFoundException {
        Users users = userRepository.findBynomorInduk(nomorInduk).orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan dengan username: " + nomorInduk));

        // Format role di Spring Security secara standar harus diawali dengan "ROLE_"
        String roleWithPrefix = "ROLE_" + users.getRole().toUpperCase();
        log.info(users.getPassword());
        return new org.springframework.security.core.userdetails.User(
            users.getNomorInduk(),
            users.getPassword(),
            Collections.singleton(() -> roleWithPrefix)
        );
    }
}
