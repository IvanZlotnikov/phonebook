package com.ivanzlotnikov.phone_book.phonebook.contact.controller;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
@RequiredArgsConstructor
public class ContactValidator {

    private final ContactService contactService;

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
