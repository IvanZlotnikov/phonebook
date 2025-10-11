package com.ivanzlotnikov.phone_book.phonebook.contact.dto;

import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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


    public static ContactDTO fromEntity(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setFullName(contact.getFullName());
        dto.setPosition(contact.getPosition());

        if (contact.getDepartment() != null) {
            dto.setDepartmentId(contact.getDepartment().getId());
            dto.setDepartmentName(contact.getDepartment().getName());
        }
        dto.setWorkPhones(new ArrayList<>(contact.getWorkPhones()));
        dto.setWorkMobilePhones(new ArrayList<>(contact.getWorkMobilePhones()));
        dto.setPersonalPhones(new ArrayList<>(contact.getPersonalPhones()));

        return dto;
    }


}
