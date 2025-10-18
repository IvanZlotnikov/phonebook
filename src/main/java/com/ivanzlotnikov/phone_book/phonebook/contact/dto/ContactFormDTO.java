package com.ivanzlotnikov.phone_book.phonebook.contact.dto;


import jakarta.validation.constraints.NotBlank;
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

    private List<String> workPhones = new ArrayList<>();
    private List<String> workMobilePhones = new ArrayList<>();
    private List<String> personalPhones = new ArrayList<>();

    public static ContactFormDTO from(ContactDTO dto) {
        ContactFormDTO formDTO = new ContactFormDTO();
        formDTO.setId(dto.getId());
        formDTO.setFullName(dto.getFullName());
        formDTO.setPosition(dto.getPosition());
        formDTO.setDepartmentId(dto.getDepartmentId());
        formDTO.setWorkPhones(dto.getWorkPhones());
        formDTO.setWorkMobilePhones(dto.getWorkMobilePhones());
        formDTO.setPersonalPhones(dto.getPersonalPhones());
        return formDTO;
    }
}
