package com.ivanzlotnikov.phone_book.phonebook.auth.service;

import com.ivanzlotnikov.phone_book.phonebook.auth.entity.User;
import com.ivanzlotnikov.phone_book.phonebook.auth.repository.UserRepository;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String name) {
        return userRepository.findByUsername(name);
    }

    public User save(User user) {
        if (user.getId() != null) {
            return updateExistingUser(user);
        } else {
            return createNewUser(user);
        }
    }

    public User createNewUser(User user) {
        user.setPassword(encodePassword(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateExistingUser(User user) {
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPassword(handlePasswordUpdate(user.getPassword(), existingUser.getPassword()));
        return userRepository.save(user);
    }

    private String handlePasswordUpdate(String newPassword, String existingPassword) {
        if (isPasswordChanged(newPassword, existingPassword)) {
            return encodePassword(newPassword);
        }
        return existingPassword;
    }

    private boolean isPasswordChanged(String newPassword, String existingPassword) {
        return newPassword != null && !newPassword.isEmpty()
               && !passwordEncoder.matches(newPassword, existingPassword);
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }


    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public long countAdmins() {
        return userRepository.countByRole("ROLE_ADMIN");
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.countByRole("ROLE_USER");
    }

}
