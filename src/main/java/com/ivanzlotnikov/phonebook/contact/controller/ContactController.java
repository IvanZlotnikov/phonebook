package com.ivanzlotnikov.phonebook.contact.controller;

import com.ivanzlotnikov.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phonebook.contact.dto.SearchContext;
import com.ivanzlotnikov.phonebook.contact.mapper.ContactMapper;
import com.ivanzlotnikov.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phonebook.department.service.DepartmentService;
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

/**
 * Контроллер для управления контактами через веб-интерфейс. Обрабатывает HTTP-запросы для операций
 * CRUD, поиска и фильтрации контактов. Использует Thymeleaf для рендеринга представлений.
 */
@Slf4j
@Controller
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int PAGINATION_WINDOW = 3;

    private static final String CONTACT_CREATED_MESSAGE = "Контакт успешно создан";
    private static final String CONTACT_UPDATED_MESSAGE = "Контакт успешно обновлен";
    private static final String CONTACT_DELETED_MESSAGE = "Контакт успешно удален";
    private static final String CONTACTS_DELETED_MESSAGE_PREFIX = "Успешно удалено контактов: ";

    private final ContactService contactService;
    private final DepartmentService departmentService;
    private final ContactMapper contactMapper;
    private final ContactValidator contactValidator;
    private final ContactRedirectBuilder redirectBuilder;

    /**
     * Отображает список всех контактов с пагинацией, поиском и фильтрацией. Поддерживает
     * комбинированный поиск по имени и департаменту.
     *
     * @param departmentId необязательный параметр для фильтрации по департаменту
     * @param searchQuery  необязательный параметр для поиска по имени
     * @param page         номер страницы (по умолчанию 0)
     * @param size         размер страницы (по умолчанию 20)
     * @param model        модель для передачи данных в представление
     * @return имя шаблона для отображения списка контактов
     */
    @GetMapping
    public String listContacts(@RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        Model model) {

        log.info("Received search request: query='{}', departmentId={}, page={}, size={}", searchQuery, departmentId, page, size);

        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);
        Pageable pageable = createPageable(page, size);
        Page<ContactDTO> contactsPage = fetchContacts(searchContext, pageable);
        List<DepartmentDTO> allDepartments = departmentService.findAll();

        log.info("Found {} contacts for search query '{}'", contactsPage.getTotalElements(), searchQuery);

        addPaginationAttributes(model, contactsPage, pageable.getPageNumber());
        addContactAttributes(model, contactsPage, allDepartments);

        return "contacts/list";
    }

    private Pageable createPageable(int page, int size) {
        int normalizedSize = (size <= 0 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
        int normalizedPage = Math.max(page, 0);
        return PageRequest.of(normalizedPage, normalizedSize, 
            Sort.by("lastName").ascending()
                .and(Sort.by("firstName").ascending())
                .and(Sort.by("middleName").ascending()));
    }

    private Page<ContactDTO> fetchContacts(SearchContext searchContext, Pageable pageable) {
        if (searchContext.hasSearchQuery() && searchContext.hasDepartment()) {
            return contactService.searchByNameAndDepartment(
                searchContext.getNormalizedSearchQuery(),
                searchContext.getDepartmentId(),
                pageable);
        } else if (searchContext.hasSearchQuery()) {
            return contactService.searchByName(searchContext.getNormalizedSearchQuery(), pageable);
        } else if (searchContext.hasDepartment()) {
            return contactService.findByDepartmentHierarchy(searchContext.getDepartmentId(),
                pageable);
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

    /**
     * Отображает форму создания нового контакта.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы создания контакта
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newContactForm(Model model) {
        addFormAttributes(model, new ContactFormDTO());
        return "contacts/form";
    }

    /**
     * Отображает форму редактирования существующего контакта.
     *
     * @param id    идентификатор контакта для редактирования
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы редактирования
     */
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

        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);
        addSearchContextToModel(model, searchContext);

        return "contacts/form";
    }

    private void addFormAttributes(Model model, ContactFormDTO contactFormDTO) {
        model.addAttribute("contact", contactFormDTO);
        model.addAttribute("departments", departmentService.findAllForForms());
    }

    /**
     * Обрабатывает сохранение контакта (создание или обновление). Выполняет валидацию и проверку на
     * дубликаты.
     *
     * @param contactFormDTO     данные контакта из формы
     * @param bindingResult      результаты валидации
     * @param searchQuery        параметр для возврата к поиску
     * @param departmentId       параметр для возврата к фильтру
     * @param page               параметр для возврата к странице
     * @param model              модель для передачи данных
     * @param redirectAttributes атрибуты для передачи сообщений после редиректа
     * @return редирект на список контактов или возврат к форме при ошибках
     */
    @PostMapping("/save")
    public String saveContact(@Valid @ModelAttribute("contact") ContactFormDTO contactFormDTO,
        BindingResult bindingResult,
        @RequestParam(value = "returnSearch", required = false) String searchQuery,
        @RequestParam(value = "returnDept", required = false) Long departmentId,
        @RequestParam(value = "returnPage", defaultValue = "0") int page,
        Model model,
        RedirectAttributes redirectAttributes) {

        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            return handleValidationError(contactFormDTO, searchContext, model);
        }
        contactValidator.checkForDuplicate(contactFormDTO);
        contactService.save(contactFormDTO);
        String message = contactFormDTO.getId() == null ?
            CONTACT_CREATED_MESSAGE : CONTACT_UPDATED_MESSAGE;
        redirectAttributes.addFlashAttribute("successMessage", message);
        return buildRedirectUrl(searchContext);
    }

    private String handleValidationError(ContactFormDTO contactFormDTO,
        SearchContext searchContext,
        Model model) {

        addFormAttributes(model, contactFormDTO);
        addSearchContextToModel(model, searchContext);
        return "contacts/form";
    }

    /**
     * Удаляет контакт по идентификатору.
     *
     * @param id                 идентификатор контакта для удаления
     * @param redirectAttributes атрибуты для передачи сообщений
     * @return редирект на список контактов
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteContact(@PathVariable Long id,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        RedirectAttributes redirectAttributes) {
        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);

        contactService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", CONTACT_DELETED_MESSAGE);
        return buildRedirectUrl(searchContext);
    }

    /**
     * Удаляет несколько контактов по списку идентификаторов.
     *
     * @param contactIds         список идентификаторов контактов для удаления
     * @param redirectAttributes атрибуты для передачи сообщений
     * @return редирект на список контактов
     */
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMultipleContacts(
        @RequestParam(value = "contactIds", required = false) List<Long> contactIds,
        @RequestParam(value = "search", required = false) String searchQuery,
        @RequestParam(value = "dept", required = false) Long departmentId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        RedirectAttributes redirectAttributes) {

        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);

        contactService.deleteAllById(contactIds);
        redirectAttributes.addFlashAttribute("successMessage",
            CONTACTS_DELETED_MESSAGE_PREFIX + contactIds.size());
        return buildRedirectUrl(searchContext);
    }


    private void addSearchContextToModel(Model model, SearchContext searchContext) {
        model.addAttribute("returnSearch", searchContext.getSearchQuery());
        model.addAttribute("returnDept", searchContext.getDepartmentId());
        model.addAttribute("returnPage", searchContext.getPage());
    }

    private String buildRedirectUrl(SearchContext searchContext) {
        return redirectBuilder.buildRedirectUrl(
            searchContext.getSearchQuery(),
            searchContext.getDepartmentId(),
            searchContext.getPage());
    }

}
