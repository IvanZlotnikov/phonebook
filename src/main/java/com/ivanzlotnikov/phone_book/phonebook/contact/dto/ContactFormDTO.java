package com.ivanzlotnikov.phone_book.phonebook.contact.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для форм создания/редактирования контакта. Используется для сбора данных из представления и
 * их валидации
 */

@Data
@NoArgsConstructor
public class ContactFormDTO {

    private Long id;

    @NotBlank(message = "ФИО не может быть пустым")
    @Size(max = 255, message = "ФИО не должно превышать 255 символов")
    private String fullName;

    @NotBlank(message = "Должность не может быть пустой")
    @Size(max = 255, message = "Должность не должна превышать 255 символов")
    private String position;

    private Long departmentId;

    @Size(max = 5, message = "Список служебных телефонов не должен содержать более 5 номеров")
    private List<@Pattern(regexp = "^[+0-9()\\-\\s]{5,20}$",
        message = "Неверный формат служебного телефона") String> workPhones = new ArrayList<>();

    @Size(max = 5, message = "Список служебных мобильных телефонов не должен содержать более 5 номеров")
    private List<@Pattern(regexp = "^[+0-9()\\-\\s]{5,20}$",
        message = "Неверный формат служебного мобильного телефона") String> workMobilePhones = new ArrayList<>();

    @Size(max = 5, message = "Список личных телефонов не должен содержать более 5 номеров")
    private List<@Pattern(regexp = "^[+0-9()\\-\\s]{5,20}$",
        message = "Неверный формат личного телефона") String> personalPhones = new ArrayList<>();

}
