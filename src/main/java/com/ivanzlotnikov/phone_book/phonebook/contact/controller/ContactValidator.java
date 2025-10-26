package com.ivanzlotnikov.phone_book.phonebook.contact.controller;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

/**
 * Валидатор для проверки контактов на дубликаты.
 * Проверяет существование контакта с аналогичными ФИО и должностью.
 */
@Component
@RequiredArgsConstructor
public class ContactValidator {

    private final ContactService contactService;

    /**
     * Проверяет, является ли контакт дубликатом.
     * Проверка выполняется только для новых контактов (без ID).
     *
     * @param contactFormDTO данные контакта для проверки
     * @param bindingResult результат валидации для добавления ошибок
     * @return true, если контакт является дубликатом
     */
    public boolean isDuplicate(ContactFormDTO contactFormDTO, BindingResult bindingResult) {
        if (contactFormDTO.getId() == null &&
            contactService.existsByFullNameAndPosition(
                                                      contactFormDTO.getFullName(),
                                                      contactFormDTO.getPosition()
            )) {
            bindingResult.rejectValue("fullName", "duplicate",
                "Контакт с таким ФИО и должностью существует");
            return true;
        }
        return false;
    }

}
