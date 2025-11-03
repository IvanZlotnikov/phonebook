package com.ivanzlotnikov.phonebook.config;

import com.ivanzlotnikov.phonebook.auth.entity.User;
import com.ivanzlotnikov.phonebook.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Загрузчик начальных данных для приложения.
 * Автоматически создает пользователей по умолчанию при первом запуске.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_USER_USERNAME = "user";
    private static final String DEFAULT_USER_PASSWORD = "password";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    /**
     * Выполняется при запуске приложения.
     * Создает пользователей по умолчанию, если они еще не существуют.
     *
     * @param args аргументы командной строки
     */
    @Override
    public void run(String... args) {
        log.info("🚀 Запуск загрузчика начальных данных...");

        createDefaultUsers();
        printUserStatistics();

        log.info("✅ Загрузка начальных данных завершена");
    }

    /**
     * Создает пользователей по умолчанию (user и admin).
     */
    private void createDefaultUsers() {
        // Создаем обычного пользователя
        if (!userRepository.existsByUsername(DEFAULT_USER_USERNAME)) {
            User user = new User();
            user.setUsername(DEFAULT_USER_USERNAME);
            user.setPassword(passwordEncoder.encode(DEFAULT_USER_PASSWORD));
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            userRepository.save(user);
            log.info("✅ Создан пользователь: {} (пароль: {})", DEFAULT_USER_USERNAME, DEFAULT_USER_PASSWORD);
        } else {
            log.info("ℹ️  Пользователь '{}' уже существует", DEFAULT_USER_USERNAME);
        }

        // Создаем администратора
        if (!userRepository.existsByUsername(DEFAULT_ADMIN_USERNAME)) {
            User admin = new User();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("✅ Создан администратор: {} (пароль: {})", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        } else {
            log.info("ℹ️  Администратор '{}' уже существует", DEFAULT_ADMIN_USERNAME);
        }
    }

    /**
     * Выводит статистику по пользователям в системе.
     */
    private void printUserStatistics() {
        long totalUsers = userRepository.count();
        long admins = userRepository.countByRole("ROLE_ADMIN");
        long users = userRepository.countByRole("ROLE_USER");

        log.info("📊 Статистика пользователей:");
        log.info("   - Всего пользователей: {}", totalUsers);
        log.info("   - Администраторов: {}", admins);
        log.info("   - Обычных пользователей: {}", users);
    }
}
