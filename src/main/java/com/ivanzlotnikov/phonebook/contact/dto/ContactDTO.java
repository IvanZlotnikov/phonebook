package com.ivanzlotnikov.phonebook.contact.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для отображения контакта в представлении.
 * Используется для передачи данных о контакте с департаментом из сервисного слоя в контроллер.
 * ФИО разделено на три отдельных поля для точного поиска и сортировки.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {

    private Long id;

    @NotBlank(message = "Фамилия обязательна для заполнения")
    private String lastName;

    @NotBlank(message = "Имя обязательно для заполнения")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Должность обязательна для заполнения")
    private String position;

    private Long departmentId;
    private String departmentName; // Только имя для отображения
    private Set<String> workPhones = new HashSet<>();
    private Set<String> workMobilePhones = new HashSet<>();
    private Set<String> personalPhones = new HashSet<>();
    
    /**
     * Возвращает полное ФИО в формате "Фамилия Имя Отчество"
     */
    public String getFullName() {
        StringBuilder result = new StringBuilder();
        if (lastName != null && !lastName.isEmpty()) {
            result.append(lastName);
        }
        if (firstName != null && !firstName.isEmpty()) {
            if (result.length() > 0) result.append(" ");
            result.append(firstName);
        }
        if (middleName != null && !middleName.isEmpty()) {
            if (result.length() > 0) result.append(" ");
            result.append(middleName);
        }
        return result.toString();
    }
    
    /**
     * Возвращает сокращенное ФИО в формате "Фамилия И.О."
     */
    public String getShortName() {
        StringBuilder result = new StringBuilder();
        if (lastName != null && !lastName.isEmpty()) {
            result.append(lastName);
        }
        if (firstName != null && !firstName.isEmpty()) {
            if (result.length() > 0) result.append(" ");
            result.append(firstName.charAt(0)).append(".");
        }
        if (middleName != null && !middleName.isEmpty()) {
            if (firstName != null && !firstName.isEmpty()) {
                result.append(middleName.charAt(0)).append(".");
            }
        }
        return result.toString();
    }
}
