package com.ivanzlotnikov.phone_book.phonebook.contact.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для отображения контакта в представлении.
 * Используется для передачи данных о контакте с департаментом из сервисного слоя в контроллер.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {

    private Long id;

    @NotBlank(message = "ФИО обязательно для заполнения")
    private String fullName;

    @NotBlank(message = "Должность обязательна для заполнения")
    private String position;

    private Long departmentId;
    private String departmentName; // Только имя для отображения
    private List<String> workPhones = new ArrayList<>();
    private List<String> workMobilePhones = new ArrayList<>();
    private List<String> personalPhones = new ArrayList<>();
}
