package com.ivanzlotnikov.phone_book.phonebook.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для формы создания/редактирования пользователя.
 * Используется для сбора данных из представления и их валидации.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFormDTO {

    private Long id;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String username;

    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    private String password;

    @NotBlank(message = "Роль не может быть пустой")
    private String role = "ROLE_USER";

    private boolean enabled = true;
}
