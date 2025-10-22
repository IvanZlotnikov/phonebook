package com.ivanzlotnikov.phone_book.phonebook.contact.controller;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.mapper.ContactMapper;
import com.ivanzlotnikov.phone_book.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final ContactMapper contactMapper;

    @GetMapping
    public String listContacts(@RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        Model model) {

        int normalizedSize = (size <= 0 || size > 100) ? 20 : size;
        int normalizedPage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize,
            Sort.by("fullName").ascending());
        Page<ContactDTO> contactsPage;

        boolean hasSearchQuery = searchQuery != null && !searchQuery.trim().isEmpty();
        boolean hasDepartment = departmentId != null;

        if (hasSearchQuery && hasDepartment) {
            // Комбинированный поиск по имени И департаменту
            contactsPage = contactService.searchByNameAndDepartment(searchQuery.trim(),
                departmentId, pageable);
        } else if (hasSearchQuery) {
            // Только по имени
            contactsPage = contactService.searchByName(searchQuery.trim(), pageable);
        } else if (hasDepartment) {
            // Только по департаменту
            contactsPage = contactService.findByDepartmentHierarchy(departmentId, pageable);
        } else {
            // Все контакты
            contactsPage = contactService.findAll(pageable);
        }

        // Для селекта департаментов и отображения названия департамента
        var allDepartments = departmentService.findAll();
        var departmentMap = allDepartments.stream()
            .collect(Collectors.toMap(DepartmentDTO::getId, DepartmentDTO::getName
            ));

        //
        int totalPages = contactsPage.getTotalPages();
        int window = 3;
        int startPage = Math.max(0, normalizedPage - window);
        int endPage = Math.min(totalPages - 1, normalizedPage + window);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // Атрибуты для шаблона
        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("contacts", contactsPage.getContent());
        model.addAttribute("page", contactsPage.getNumber());
        model.addAttribute("size", contactsPage.getSize());
        model.addAttribute("totalPages", contactsPage.getTotalPages());
        model.addAttribute("totalElements", contactsPage.getTotalElements());
        model.addAttribute("departments", allDepartments);
        model.addAttribute("departmentMap", departmentMap);

        return "contacts/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newContactForm(Model model) {
        model.addAttribute("contact", new ContactFormDTO());
        model.addAttribute("departments", departmentService.findAllForForms());
        return "contacts/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editContactForm(@PathVariable Long id, Model model) {
        ContactDTO contactDTO = contactService.findById(id);
        ContactFormDTO contactFormDTO = contactMapper.toFormDTO(contactDTO);

        model.addAttribute("contact", contactFormDTO);
        model.addAttribute("departments", departmentService.findAllForForms());
        return "contacts/form";
    }

    @PostMapping("/save")
    public String saveContact(@Valid @ModelAttribute("contact") ContactFormDTO contactFormDTO,
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
            // Проверка дубликатов только для новых контактов
            if (contactFormDTO.getId() == null &&
                contactService.existsByFullNameAndPosition(
                    contactFormDTO.getFullName(), contactFormDTO.getPosition())) {
                bindingResult.rejectValue("fullName", "duplicate",
                    "Контакт с таким ФИО и должностью уже существует");
                model.addAttribute("departments", departmentService.findAllForForms());
                return "contacts/form";
            }
            contactService.save(contactFormDTO);

            String message = contactFormDTO.getId() == null ?
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
