package com.ivanzlotnikov.phonebook.auth.repository;

import com.ivanzlotnikov.phonebook.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * Предоставляет методы для аутентификации и управления пользователями системы.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени.
     * Используется для аутентификации в Spring Security.
     *
     * @param username имя пользователя
     * @return Optional с пользователем или пустой Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным именем.
     *
     * @param username имя пользователя
     * @return true, если пользователь существует, иначе false
     */
    boolean existsByUsername(String username);

    /**
     * Подсчитывает количество пользователей с указанной ролью.
     *
     * @param role роль пользователя (например, "ROLE_USER", "ROLE_ADMIN")
     * @return количество пользователей с данной ролью
     */
    long countByRole(String role);

}
