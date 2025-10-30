package com.ivanzlotnikov.phonebook.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность пользователя системы для аутентификации и авторизации.
 * Содержит учетные данные пользователя, роль и статус активности.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private static final int USERNAME_FIELD_LENGTH = 50;
    private static final int PASSWORD_FIELD_LENGTH = 100;
    private static final String DEFAULT_ROLE = "ROLE_USER";

    /**
     * Уникальный идентификатор пользователя.
     */
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя для входа в систему.
     * Обязательное уникальное поле, максимальная длина 50 символов.
     */
    @NotBlank
    @Column(nullable = false, unique = true, length = USERNAME_FIELD_LENGTH)
    private String username;

    /**
     * Хешированный пароль пользователя.
     * Обязательное поле, максимальная длина 100 символов.
     * Хранится в зашифрованном виде с использованием BCrypt.
     */
    @NotBlank
    @Column(nullable = false, length = PASSWORD_FIELD_LENGTH)
    private String password;

    /**
     * Роль пользователя в системе.
     * По умолчанию "ROLE_USER".
     * Используется для контроля доступа к функциям приложения.
     */
    @Column(nullable = false)
    private String role = DEFAULT_ROLE;

    /**
     * Флаг активности пользователя.
     * По умолчанию true. Неактивные пользователи не могут войти в систему.
     */
    private boolean enabled = true;
}
