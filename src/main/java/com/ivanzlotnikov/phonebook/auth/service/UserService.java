package com.ivanzlotnikov.phonebook.auth.service;

import com.ivanzlotnikov.phonebook.auth.entity.User;
import com.ivanzlotnikov.phonebook.auth.repository.UserRepository;
import com.ivanzlotnikov.phonebook.exception.DuplicateResourceException;
import com.ivanzlotnikov.phonebook.exception.InvalidDataException;
import com.ivanzlotnikov.phonebook.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления пользователями системы. Предоставляет операции CRUD, управление паролями и
 * проверку существования пользователей.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String PASSWORD_MANDATORY_MESSAGE = "Пароль обязателен при создании пользователя";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Получает список всех пользователей.
     *
     * @return список всех пользователей
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return Optional с пользователем или пустой Optional
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Находит пользователя по имени.
     *
     * @param name имя пользователя
     * @return Optional с пользователем или пустой Optional
     */
    public Optional<User> findByUsername(String name) {
        return userRepository.findByUsername(name);
    }

    /**
     * Сохраняет пользователя (создает нового или обновляет существующего).
     *
     * @param user данные пользователя
     * @return сохраненный пользователь
     */
    public User save(User user) {
        if (user.getId() != null) {
            return updateExistingUser(user);
        } else {
            return createNewUser(user);
        }
    }

    /**
     * Создает нового пользователя с шифрованием пароля.
     *
     * @param user данные нового пользователя
     * @return созданный пользователь
     */
    public User createNewUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw DuplicateResourceException.of("Пользователь", "username", user.getUsername());
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw InvalidDataException.forField("password",
                PASSWORD_MANDATORY_MESSAGE);
        }
        user.setPassword(encodePassword(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param user данные пользователя для обновления
     * @return обновленный пользователь
     * @throws ResourceNotFoundException если пользователь не найден
     */
    public User updateExistingUser(User user) {
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() ->
                ResourceNotFoundException.byId("Пользователь",
                    user.getId()));
        user.setPassword(handlePasswordUpdate(user.getPassword(), existingUser.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Обрабатывает обновление пароля пользователя.
     *
     * @param newPassword      новый пароль
     * @param existingPassword существующий зашифрованный пароль
     * @return зашифрованный пароль (новый или существующий)
     */
    private String handlePasswordUpdate(String newPassword, String existingPassword) {
        if (isPasswordChanged(newPassword, existingPassword)) {
            return encodePassword(newPassword);
        }
        return existingPassword;
    }

    /**
     * Проверяет, был ли изменен пароль.
     *
     * @param newPassword      новый пароль
     * @param existingPassword существующий зашифрованный пароль
     * @return true, если пароль был изменен
     */
    private boolean isPasswordChanged(String newPassword, String existingPassword) {
        return newPassword != null && !newPassword.isEmpty()
               && !passwordEncoder.matches(newPassword, existingPassword);
    }

    /**
     * Шифрует пароль с использованием BCrypt.
     *
     * @param rawPassword незашифрованный пароль
     * @return зашифрованный пароль
     */
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @throws ResourceNotFoundException если пользователь не найден
     */
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException.byId("Пользователь", id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Проверяет существование пользователя с указанным именем.
     *
     * @param username имя пользователя
     * @return true, если пользователь существует
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Подсчитывает количество администраторов в системе.
     *
     * @return количество пользователей с ролью ADMIN
     */
    @Transactional(readOnly = true)
    public long countAdmins() {
        return userRepository.countByRole(ROLE_ADMIN);
    }

    /**
     * Подсчитывает количество обычных пользователей в системе.
     *
     * @return количество пользователей с ролью USER
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.countByRole(ROLE_USER);
    }

}
