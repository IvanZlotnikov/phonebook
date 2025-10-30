package com.ivanzlotnikov.phonebook.auth.mapper;

import com.ivanzlotnikov.phonebook.auth.dto.UserDTO;
import com.ivanzlotnikov.phonebook.auth.dto.UserFormDTO;
import com.ivanzlotnikov.phonebook.auth.entity.User;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования сущности User в DTO и обратно.
 * Выполняет конвертацию между слоями приложения.
 */
@Component
public class UserMapper {

    /**
     * Преобразует сущность User в DTO для отображения.
     *
     * @param user сущность пользователя
     * @return DTO пользователя или null
     */
    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        return dto;
    }

    /**
     * Преобразует UserFormDTO в сущность User.
     *
     * @param formDTO DTO с данными из формы
     * @return сущность пользователя или null
     */
    public User toEntity(UserFormDTO formDTO) {
        if (formDTO == null) {
            return null;
        }
        User user = new User();
        user.setId(formDTO.getId());
        user.setUsername(formDTO.getUsername());
        user.setPassword(formDTO.getPassword());
        user.setRole(formDTO.getRole());
        user.setEnabled(formDTO.isEnabled());
        return user;
    }

    /**
     * Преобразует UserDTO в UserFormDTO для редактирования.
     *
     * @param dto DTO пользователя
     * @return DTO для формы или null
     */
    public UserFormDTO toFormDTO(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        UserFormDTO formDTO = new UserFormDTO();
        formDTO.setId(dto.getId());
        formDTO.setUsername(dto.getUsername());
        formDTO.setRole(dto.getRole());
        formDTO.setEnabled(dto.isEnabled());
        // Пароль не передаем при редактировании
        return formDTO;
    }

    /**
     * Преобразует User в UserFormDTO для редактирования.
     *
     * @param user сущность пользователя
     * @return DTO для формы или null
     */
    public UserFormDTO userToFormDTO(User user) {
        if (user == null) {
            return null;
        }
        UserFormDTO formDTO = new UserFormDTO();
        formDTO.setId(user.getId());
        formDTO.setUsername(user.getUsername());
        formDTO.setRole(user.getRole());
        formDTO.setEnabled(user.isEnabled());
        // Пароль не передаем при редактировании
        return formDTO;
    }
}
