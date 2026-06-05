package lsp.gunadarma.nilaimhs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import lsp.gunadarma.nilaimhs.entity.Users;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, String>{
    Optional<Users> findBynomorInduk(String nomor_induk);
}
