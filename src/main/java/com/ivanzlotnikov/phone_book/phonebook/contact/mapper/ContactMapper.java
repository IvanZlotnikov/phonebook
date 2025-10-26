package com.ivanzlotnikov.phone_book.phonebook.contact.mapper;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования сущности Contact в DTO и обратно.
 * Выполняет конвертацию между слоями приложения.
 */
@Component
public class ContactMapper {

    /**
     * Преобразует сущность Contact в DTO для отображения.
     *
     * @param entity сущность контакта
     * @return DTO контакта или null
     */
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

    /**
     * Преобразует FormDTO в сущность Contact для сохранения.
     *
     * @param formDTO DTO с данными из формы
     * @return сущность контакта или null
     */
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

    /**
     * Преобразует ContactDTO в ContactFormDTO для редактирования.
     *
     * @param dto DTO контакта для отображения
     * @return DTO для формы редактирования или null
     */
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
