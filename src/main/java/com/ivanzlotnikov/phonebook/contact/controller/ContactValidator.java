package com.ivanzlotnikov.phonebook.contact.controller;

import com.ivanzlotnikov.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phonebook.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Валидатор для проверки контактов на дубликаты. Проверяет существование контакта с аналогичными
 * ФИО и должностью.
 */
@Component
@RequiredArgsConstructor
public class ContactValidator {

    private final ContactService contactService;

    /**
     * Проверяет, является ли контакт дубликатом. Проверка выполняется только для новых контактов
     * (без ID).
     *
     * @param contactFormDTO данные контакта для проверки
     * @throws DuplicateResourceException если контакт уже существует
     */
    public void checkForDuplicate(ContactFormDTO contactFormDTO) {
        if (contactFormDTO.getId() == null &&
            contactService.existsByNameAndPosition(
                contactFormDTO.getLastName(),
                contactFormDTO.getFirstName(),
                contactFormDTO.getMiddleName(),
                contactFormDTO.getPosition()
            )) {
            String fullName = contactFormDTO.getLastName() + " " + 
                             contactFormDTO.getFirstName() + 
                             (contactFormDTO.getMiddleName() != null ? " " + contactFormDTO.getMiddleName() : "");
            throw DuplicateResourceException.of(
                "Контакт", "ФИО и должность",
                fullName + " / " + contactFormDTO.getPosition());
        }
    }
}
