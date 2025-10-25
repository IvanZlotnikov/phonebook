package com.ivanzlotnikov.phone_book.phonebook.contact.mapper;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    //Маппинг entity в dto(отображение)
    public ContactDTO toDto(Contact entity) {
        if (entity == null) {
            return null;
        }
        ContactDTO dto = new ContactDTO();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setPosition(entity.getPosition());
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        dto.setWorkPhones(entity.getWorkPhones());
        dto.setWorkMobilePhones(entity.getWorkMobilePhones());
        dto.setPersonalPhones(entity.getPersonalPhones());
        return dto;
    }

    //Маппинг FormDTO в Entity(сохранение/обновление)
    public Contact toEntity(ContactFormDTO formDTO) {
        if (formDTO == null) {
            return null;
        }
        Contact entity = new Contact();
        entity.setId(formDTO.getId());
        entity.setFullName(formDTO.getFullName());
        entity.setPosition(formDTO.getPosition());

        entity.setWorkPhones(formDTO.getWorkPhones());
        entity.setWorkMobilePhones(formDTO.getWorkMobilePhones());
        entity.setPersonalPhones(formDTO.getPersonalPhones());
        return entity;
    }

    // Маппинг DTO в FormDTO(редактирование)
    public ContactFormDTO toFormDTO(ContactDTO dto) {
        if (dto == null) {
            return null;
        }
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
