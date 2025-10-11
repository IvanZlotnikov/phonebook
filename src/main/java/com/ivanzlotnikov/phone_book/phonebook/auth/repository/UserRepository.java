package com.ivanzlotnikov.phone_book.phonebook.auth.repository;

import com.ivanzlotnikov.phone_book.phonebook.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRole(String role);

}
