package com.ivanzlotnikov.phone_book.phonebook.auth.service;

import com.ivanzlotnikov.phone_book.phonebook.auth.entity.User;
import com.ivanzlotnikov.phone_book.phonebook.auth.repository.UserRepository;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
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
            User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

            String rawPassword = user.getPassword();
            if (rawPassword != null && !rawPassword.isEmpty() &&
                passwordEncoder.matches(rawPassword, existingUser.getPassword())) {
                // Пароль изменился - кодируем новый
                user.setPassword(passwordEncoder.encode(rawPassword));
            } else {
                // Пароль не изменился - сохраняем старый
                user.setPassword(existingUser.getPassword());
            }
        } else {
            // Новый пользователь - всегда кодируем пароль
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
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
