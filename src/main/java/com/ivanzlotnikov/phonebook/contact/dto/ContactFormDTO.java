package com.ivanzlotnikov.phonebook.contact.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для форм создания/редактирования контакта. Используется для сбора данных из представления и
 * их валидации.
 * ФИО разделено на три поля для точного поиска.
 */

@Data
@NoArgsConstructor
public class ContactFormDTO {

    private Long id;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String lastName;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String firstName;

    @Size(max = 100, message = "Отчество не должно превышать 100 символов")
    private String middleName;

    @NotBlank(message = "Должность не может быть пустой")
    @Size(max = 255, message = "Должность не должна превышать 255 символов")
    private String position;

    private Long departmentId;

    @Size(max = 5, message = "Список служебных телефонов не должен содержать более 5 номеров")
    private Set<@Pattern(regexp = "^[+0-9()\\-\\s]{5,20}$",
        message = "Неверный формат служебного телефона") String> workPhones = new HashSet<>();

    @Size(max = 5, message = "Список служебных мобильных телефонов не должен содержать более 5 номеров")
    private Set<@Pattern(regexp = "^[+0-9()\\-\\s]{5,20}$",
        message = "Неверный формат служебного мобильного телефона") String> workMobilePhones = new HashSet<>();

    @Size(max = 5, message = "Список личных телефонов не должен содержать более 5 номеров")
    private Set<@Pattern(regexp = "^[+0-9()\\-\\s]{5,20}$",
        message = "Неверный формат личного телефона") String> personalPhones = new HashSet<>();

}
