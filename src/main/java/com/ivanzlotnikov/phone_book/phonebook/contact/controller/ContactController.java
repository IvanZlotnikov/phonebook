package com.ivanzlotnikov.phone_book.phonebook.contact.controller;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final DepartmentService departmentService;

    @GetMapping
    public String listContacts(@RequestParam(value = "dept", required = false) Long departmentId,
                               @RequestParam(value = "search", required = false) String searchQuery,
                               Model model) {
        log.info("Listing contacts, departmentId: {}, searchQuery: {}", departmentId, searchQuery);

        List<ContactDTO> contacts;
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            contacts = contactService.searchByName(searchQuery.trim());
        } else if (departmentId != null) {
            contacts = contactService.findByDepartmentHierarchy(departmentId);
        } else {
            contacts = contactService.findAll();
        }

        //Создаем map для быстрого доступа к названиям подразделений
        List<DepartmentDTO> allDepartments = departmentService.findAll();
        Map<Long, String> departmentMap = allDepartments.stream()
            .collect(Collectors.toMap(DepartmentDTO::getId, DepartmentDTO::getName));

        model.addAttribute("contacts", contacts);
        model.addAttribute("departments", allDepartments);
        model.addAttribute("departmentMap", departmentMap);
        model.addAttribute("totalContacts", contactService.count());

        return "contacts/list";
    }

    @GetMapping("/new")
    public String showContactForm(Model model) {
        model.addAttribute("contact", new ContactDTO());
        model.addAttribute("departments", departmentService.findAllForForms());
        return "contacts/form";
    }

    @GetMapping("/edit/{id}")
    public String editContact(@PathVariable Long id, Model model) {
        ContactDTO contact = contactService.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contact not found with id: " + id));

        model.addAttribute("contact", contact);
        model.addAttribute("departments", departmentService.findAllForForms());
        return "contacts/form";
    }

    @PostMapping("/save")
    public String saveContact(
        @RequestParam(value = "workPhones", required = false) List<String> workPhones,
        @RequestParam(value = "workMobilePhones", required = false) List<String> workMobilePhones,
        @RequestParam(value = "personalPhones", required = false) List<String> personalPhones,
        @Valid @ModelAttribute("contact") ContactDTO contactDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("departments", departmentService.findAllForForms());
            return "contacts/form";
        }

        try {
            // Устанавливаем телефоны из параметров
            contactDTO.setWorkPhones(workPhones != null ? workPhones : List.of());
            contactDTO.setWorkMobilePhones(workMobilePhones != null ? workMobilePhones : List.of());
            contactDTO.setPersonalPhones(personalPhones != null ? personalPhones : List.of());

            // Проверка дубликатов только для новых контактов
            if (contactDTO.getId() == null &&
                contactService.existsByFullNameAndPosition(
                    contactDTO.getFullName(), contactDTO.getPosition())) {
                bindingResult.rejectValue("fullName", "duplicate",
                    "Контакт с таким ФИО и должностью уже существует");
                model.addAttribute("departments", departmentService.findAllForForms());
                return "contacts/form";
            }

            ContactDTO savedContact = contactService.save(contactDTO);

            String message = contactDTO.getId() == null ?
                "Контакт успешно создан" : "Контакт успешно обновлен";
            redirectAttributes.addFlashAttribute("successMessage", message);

            return "redirect:/contacts";
        } catch (Exception e) {
            log.error("Error saving contact", e);
            model.addAttribute("errorMessage", "Ошибка при сохранении контакта: " + e.getMessage());
            model.addAttribute("departments", departmentService.findAllForForms());
            return "contacts/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteContact(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            contactService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Контакт успешно удален");
        } catch (EntityNotFoundException e) {
            log.warn("Attempt to delete non-existing contact with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Ошибка при удалении контакта: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

    @PostMapping("/delete")
    public String deleteMultipleContacts(
        @RequestParam(value = "contactIds", required = false) List<Long> contactIds,
        RedirectAttributes redirectAttributes) {
        try {
            if (contactIds == null || contactIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Не выбраны контакты для удаления");
                return "redirect:/contacts";
            }

            contactService.deleteAllById(contactIds);
            redirectAttributes.addFlashAttribute("successMessage",
                "Успешно удалено контактов: " + contactIds.size());
        } catch (Exception e) {
            log.error("Error deleting multiple contacts: {}", contactIds, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Ошибка при удалении контактов: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

}
