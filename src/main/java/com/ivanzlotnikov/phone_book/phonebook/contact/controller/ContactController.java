package com.ivanzlotnikov.phone_book.phonebook.contact.controller;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.mapper.ContactMapper;
import com.ivanzlotnikov.phone_book.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int PAGINATION_WINDOW = 3;

    private final ContactService contactService;
    private final DepartmentService departmentService;
    private final ContactMapper contactMapper;
    private final ContactValidator contactValidator;
    private final ContactRedirectBuilder redirectBuilder;


    @GetMapping
    public String listContacts(@RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        Model model) {

        Pageable pageable = createPageable(page, size);
        Page<ContactDTO> contactsPage = fetchContacts(departmentId, searchQuery, pageable);
        List<DepartmentDTO> allDepartments = departmentService.findAll();

        addPaginationAttributes(model, contactsPage, pageable.getPageNumber());
        addContactAttributes(model, contactsPage, allDepartments);

        return "contacts/list";
    }

    private Pageable createPageable(int page, int size) {
        int normalizedSize = (size <= 0 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
        int normalizedPage = Math.max(page, 0);
        return PageRequest.of(normalizedPage, normalizedSize, Sort.by("fullName").ascending());
    }

    private Page<ContactDTO> fetchContacts(Long departmentId, String searchQuery,
        Pageable pageable) {
        boolean hasSearchQuery = searchQuery != null && !searchQuery.trim().isEmpty();
        boolean hasDepartment = departmentId != null;

        if (hasSearchQuery && hasDepartment) {
            return contactService.searchByNameAndDepartment(searchQuery.trim(), departmentId,
                pageable);
        } else if (hasSearchQuery) {
            return contactService.searchByName(searchQuery.trim(), pageable);
        } else if (hasDepartment) {
            return contactService.findByDepartmentHierarchy(departmentId, pageable);
        }
        return contactService.findAll(pageable);
    }

    private void addPaginationAttributes(Model model, Page<ContactDTO> page, int currentPage) {
        int totalPages = page.getTotalPages();
        int startPage = Math.max(0, currentPage - PAGINATION_WINDOW);
        int endPage = Math.min(totalPages - 1, currentPage + PAGINATION_WINDOW);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("page", page.getNumber());
        model.addAttribute("size", page.getSize());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", page.getTotalElements());
    }

    private void addContactAttributes(Model model, Page<ContactDTO> contactsPage,
        List<DepartmentDTO> departments) {
        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("contacts", contactsPage.getContent());
        model.addAttribute("departments", departments);
        model.addAttribute("departmentMap", departments.stream()
            .collect(Collectors.toMap(DepartmentDTO::getId, DepartmentDTO::getName)));
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newContactForm(Model model) {
        addFormAttributes(model, new ContactFormDTO());
        return "contacts/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editContactForm(@PathVariable Long id,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        Model model) {
        ContactDTO contactDTO = contactService.findById(id);
        ContactFormDTO formDTO = contactMapper.toFormDTO(contactDTO);
        addFormAttributes(model, formDTO);

        // Сохраняем параметры поиска для возврата
        model.addAttribute("returnSearch", searchQuery);
        model.addAttribute("returnDept", departmentId);
        model.addAttribute("returnPage", page);

        return "contacts/form";
    }

    private void addFormAttributes(Model model, ContactFormDTO contactFormDTO) {
        model.addAttribute("contact", contactFormDTO);
        model.addAttribute("departments", departmentService.findAllForForms());
    }

    @PostMapping("/save")
    public String saveContact(@Valid @ModelAttribute("contact") ContactFormDTO contactFormDTO,
        BindingResult bindingResult,
        @RequestParam(value = "returnSearch", required = false) String searchQuery,
        @RequestParam(value = "returnDept", required = false) Long departmentId,
        @RequestParam(value = "returnPage", defaultValue = "0") int page,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            return handleValidationError(contactFormDTO, searchQuery, departmentId, page, model);
        }
        if (contactValidator.isDuplicate(contactFormDTO, bindingResult)) {
            return handleValidationError(contactFormDTO, searchQuery, departmentId, page, model);
        }
        return saveContactAndRedirect(contactFormDTO, searchQuery, departmentId, page,
            redirectAttributes);
    }

    private String handleValidationError(ContactFormDTO contactFormDTO,
        String searchQuery,
        Long departmentId,
        int page,
        Model model) {
        addFormAttributes(model, contactFormDTO);
        preserveSearchParams(model, searchQuery, departmentId, page);
        return "contacts/form";
    }

    private String saveContactAndRedirect(@Valid ContactFormDTO contactFormDTO,
        String searchQuery,
        Long departmentId,
        int page,
        RedirectAttributes redirectAttributes) {
        try {
            contactService.save(contactFormDTO);
            String message = contactFormDTO.getId() == null ?
                "Контакт успешно создан" : "Контакт успешно обновлен";
            redirectAttributes.addFlashAttribute("successMessage", message);
            return redirectBuilder.buildRedirectUrl(searchQuery, departmentId, page);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении контакта", e);
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteContact(@PathVariable Long id,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        RedirectAttributes redirectAttributes) {
        try {
            contactService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Контакт успешно удален");
        } catch (EntityNotFoundException e) {
            log.warn("Attempt to delete non-existing contact with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Ошибка при удалении контакта: " + e.getMessage());
        }
        return redirectBuilder.buildRedirectUrl(searchQuery, departmentId, page);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMultipleContacts(
        @RequestParam(value = "contactIds", required = false) List<Long> contactIds,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        RedirectAttributes redirectAttributes) {
        try {
            if (contactIds == null || contactIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Не выбраны контакты для удаления");
                return redirectBuilder.buildRedirectUrl(searchQuery, departmentId, page);
            }

            contactService.deleteAllById(contactIds);
            redirectAttributes.addFlashAttribute("successMessage",
                "Успешно удалено контактов: " + contactIds.size());
        } catch (Exception e) {
            log.error("Error deleting multiple contacts: {}", contactIds, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Ошибка при удалении контактов: " + e.getMessage());
        }
        return redirectBuilder.buildRedirectUrl(searchQuery, departmentId, page);
    }


    private void preserveSearchParams(Model model,
        String searchQuery,
        Long departmentId,
        Integer page) {
        model.addAttribute("returnSearch", searchQuery);
        model.addAttribute("returnDept", departmentId);
        model.addAttribute("returnPage", page);
    }

}
