package com.ivanzlotnikov.phonebook.contact.controller;

import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.CONTACTS_DELETED_MSG_PREFIX;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.CONTACT_CREATED_MSG;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.CONTACT_DELETED_MSG;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.CONTACT_UPDATED_MSG;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.DEFAULT_PAGE_SIZE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.DEFAULT_PAGE_SIZE_STR;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.FIELD_FIRST_NAME;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.FIELD_LAST_NAME;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.FIELD_MIDDLE_NAME;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MAX_PAGE_SIZE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_CONTACT;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_CONTACTS;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_CONTACTS_PAGE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_DEPARTMENTS;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_DEPARTMENT_MAP;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_END_PAGE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_PAGE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_SIZE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_START_PAGE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_TOTAL_ELEMENTS;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.MODEL_TOTAL_PAGES;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PAGINATION_WINDOW;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_CONTACT_IDS;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_DEPT;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_PAGE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_RETURN_DEPT;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_RETURN_PAGE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_RETURN_SEARCH;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_SEARCH;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.PARAM_SIZE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.URL_CONTACTS;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.URL_DELETE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.URL_EDIT;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.URL_NEW;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.URL_SAVE;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.VIEW_CONTACTS_LIST;
import static com.ivanzlotnikov.phonebook.contact.constants.ContactConstants.VIEW_CONTACT_FORM;

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
@RequestMapping(URL_CONTACTS)
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final DepartmentService departmentService;
    private final ContactMapper contactMapper;
    private final ContactValidator contactValidator;
    private final ContactRedirectBuilder redirectBuilder;
    /** Отображает список контактов с возможностью фильтрации и поиска.
     *
     * @param departmentId ID отдела для фильтрации контактов (опционально)
     * @param searchQuery  поисковый запрос для фильтрации контактов (опционально)
     * @param page         номер страницы (начинается с 0)
     * @param size         количество элементов на странице
     * @param model        модель для передачи данных в представление
     * @return имя представления для отображения списка контактов
     */
    @GetMapping
    public String listContacts(
        @RequestParam(value = PARAM_DEPT, required = false) Long departmentId,
        @RequestParam(value = PARAM_SEARCH, required = false) String searchQuery,
        @RequestParam(value = PARAM_PAGE, defaultValue = "0") int page,
        @RequestParam(value = PARAM_SIZE, defaultValue = DEFAULT_PAGE_SIZE_STR) int size,
        Model model) {

        log.info("Received search request: query='{}', departmentId={}, page={}, size={}",
            searchQuery, departmentId, page, size);

        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);
        Pageable pageable = createPageable(page, size);
        Page<ContactDTO> contactsPage = fetchContacts(searchContext, pageable);
        List<DepartmentDTO> allDepartments = departmentService.findAll();

        log.info("Found {} contacts for search query '{}'", contactsPage.getTotalElements(),
            searchQuery);

        addPaginationAttributes(model, contactsPage, pageable.getPageNumber());
        addContactAttributes(model, contactsPage, allDepartments);

        return VIEW_CONTACTS_LIST;
    }

    /**
     * Создает объект Pageable с настройками пагинации и сортировки.
     *
     * @param page номер страницы (начинается с 0)
     * @param size количество элементов на странице
     * @return объект Pageable с настройками пагинации и сортировки
     */
    private Pageable createPageable(int page, int size) {
        int normalizedSize = (size <= 0 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
        int normalizedPage = Math.max(page, 0);
        return PageRequest.of(normalizedPage, normalizedSize,
            Sort.by(FIELD_LAST_NAME).ascending()
                .and(Sort.by(FIELD_FIRST_NAME).ascending())
                .and(Sort.by(FIELD_MIDDLE_NAME).ascending()));
    }

    /**
     * Получает страницу контактов в соответствии с параметрами поиска и фильтрации.
     *
     * @param searchContext контекст поиска, содержащий параметры фильтрации
     * @param pageable      параметры пагинации и сортировки
     * @return страница с найденными контактами
     */
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

    /**
     * Добавляет атрибуты пагинации в модель.
     *
     * @param model       модель для добавления атрибутов
     * @param page        страница с данными
     * @param currentPage текущая страница
     */
    private void addPaginationAttributes(Model model, Page<?> page, int currentPage) {
        int totalPages = page.getTotalPages();
        int startPage = Math.max(0, currentPage - PAGINATION_WINDOW);
        int endPage = Math.min(totalPages - 1, currentPage + PAGINATION_WINDOW);

        model.addAttribute(MODEL_START_PAGE, startPage);
        model.addAttribute(MODEL_END_PAGE, endPage);
        model.addAttribute(MODEL_PAGE, page.getNumber());
        model.addAttribute(MODEL_SIZE, page.getSize());
        model.addAttribute(MODEL_TOTAL_PAGES, totalPages);
        model.addAttribute(MODEL_TOTAL_ELEMENTS, page.getTotalElements());
    }

    /**
     * Добавляет атрибуты контактов и отделов в модель.
     *
     * @param model        модель для добавления атрибутов
     * @param contactsPage страница с контактами
     * @param departments  список всех отделов
     */
    private void addContactAttributes(Model model, Page<ContactDTO> contactsPage,
        List<DepartmentDTO> departments) {
        model.addAttribute(MODEL_CONTACTS_PAGE, contactsPage);
        model.addAttribute(MODEL_CONTACTS, contactsPage.getContent());
        model.addAttribute(MODEL_DEPARTMENTS, departments);
        model.addAttribute(MODEL_DEPARTMENT_MAP, departments.stream()
            .collect(Collectors.toMap(DepartmentDTO::getId, DepartmentDTO::getName)));
    }

    /**
     * Отображает форму для создания нового контакта. Доступно только для пользователей с ролью
     * ADMIN.
     *
     * @param model модель для передачи данных в представление
     * @return имя представления формы контакта
     */
    @GetMapping(URL_NEW)
    @PreAuthorize("hasRole('ADMIN')")
    public String newContactForm(Model model) {
        addFormAttributes(model, new ContactFormDTO());
        return VIEW_CONTACT_FORM;
    }

    /**
     * Отображает форму для редактирования существующего контакта. Доступно только для пользователей
     * с ролью ADMIN.
     *
     * @param id           идентификатор редактируемого контакта
     * @param searchQuery  поисковый запрос для возврата на предыдущую страницу (опционально)
     * @param departmentId ID отдела для возврата на предыдущую страницу (опционально)
     * @param page         номер страницы для возврата (по умолчанию 0)
     * @param model        модель для передачи данных в представление
     * @return имя представления формы контакта
     */
    @GetMapping(URL_EDIT + "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editContactForm(@PathVariable Long id,
        @RequestParam(value = PARAM_SEARCH, required = false) String searchQuery,
        @RequestParam(value = PARAM_DEPT, required = false) Long departmentId,
        @RequestParam(value = PARAM_PAGE, defaultValue = "0") int page,
        Model model) {
        ContactDTO contactDTO = contactService.findById(id);
        ContactFormDTO formDTO = contactMapper.toFormDTO(contactDTO);
        addFormAttributes(model, formDTO);

        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);
        addSearchContextToModel(model, searchContext);

        return VIEW_CONTACT_FORM;
    }

    /**
     * Добавляет атрибуты формы контакта в модель.
     *
     * @param model          модель для добавления атрибутов
     * @param contactFormDTO данные контакта для формы
     */
    private void addFormAttributes(Model model, ContactFormDTO contactFormDTO) {
        model.addAttribute(MODEL_CONTACT, contactFormDTO);
        model.addAttribute(MODEL_DEPARTMENTS, departmentService.findAllForForms());
    }

    /**
     * Сохраняет или обновляет контакт в базе данных. Доступно только для пользователей с ролью
     * ADMIN.
     *
     * @param contactFormDTO     данные контакта для сохранения
     * @param bindingResult      результаты валидации формы
     * @param searchQuery        поисковый запрос для возврата на предыдущую страницу (опционально)
     * @param departmentId       ID отдела для возврата на предыдущую страницу (опционально)
     * @param page               номер страницы для возврата (по умолчанию 0)
     * @param model              модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return строка перенаправления на страницу со списком контактов или форму при ошибке
     */
    @PostMapping(URL_SAVE)
    @PreAuthorize("hasRole('ADMIN')")
    public String saveContact(@Valid @ModelAttribute(MODEL_CONTACT) ContactFormDTO contactFormDTO,
        BindingResult bindingResult,
        @RequestParam(value = PARAM_RETURN_SEARCH, required = false) String searchQuery,
        @RequestParam(value = PARAM_RETURN_DEPT, required = false) Long departmentId,
        @RequestParam(value = PARAM_RETURN_PAGE, defaultValue = "0") int page,
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
            CONTACT_CREATED_MSG : CONTACT_UPDATED_MSG;
        redirectAttributes.addFlashAttribute("successMessage", message);

        return buildRedirectUrl(searchContext);
    }

    private String handleValidationError(ContactFormDTO contactFormDTO,
        SearchContext searchContext,
        Model model) {
        addFormAttributes(model, contactFormDTO);
        addSearchContextToModel(model, searchContext);
        return VIEW_CONTACT_FORM;
    }

    /**
     * Удаляет контакт по его идентификатору. Доступно только для пользователей с ролью ADMIN.
     *
     * @param id                 идентификатор контакта для удаления
     * @param searchQuery        поисковый запрос для возврата на предыдущую страницу (опционально)
     * @param departmentId       ID отдела для возврата на предыдущую страницу (опционально)
     * @param page               номер страницы для возврата (по умолчанию 0)
     * @param redirectAttributes атрибуты для редиректа
     * @return строка перенаправления на страницу со списком контактов
     */
    @PostMapping(URL_DELETE + "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteContact(@PathVariable Long id,
        @RequestParam(value = PARAM_SEARCH, required = false) String searchQuery,
        @RequestParam(value = PARAM_DEPT, required = false) Long departmentId,
        @RequestParam(value = PARAM_PAGE, defaultValue = "0") int page,
        RedirectAttributes redirectAttributes) {
        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);

        contactService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", CONTACT_DELETED_MSG);

        return buildRedirectUrl(searchContext);
    }

    @PostMapping(URL_DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMultipleContacts(
        @RequestParam(value = PARAM_CONTACT_IDS, required = false) List<Long> contactIds,
        @RequestParam(value = PARAM_SEARCH, required = false) String searchQuery,
        @RequestParam(value = PARAM_DEPT, required = false) Long departmentId,
        @RequestParam(value = PARAM_PAGE, defaultValue = "0") int page,
        RedirectAttributes redirectAttributes) {

        SearchContext searchContext = new SearchContext(searchQuery, departmentId, page);

        if (contactIds == null || contactIds.isEmpty()) {
            return buildRedirectUrl(searchContext);
        }

        contactService.deleteAllById(contactIds);
        redirectAttributes.addFlashAttribute("successMessage",
            CONTACTS_DELETED_MSG_PREFIX + contactIds.size());

        return buildRedirectUrl(searchContext);
    }

    private void addSearchContextToModel(Model model, SearchContext searchContext) {
        model.addAttribute(PARAM_SEARCH, searchContext.getSearchQuery());
        model.addAttribute(PARAM_DEPT, searchContext.getDepartmentId());
        model.addAttribute(PARAM_PAGE, searchContext.getPage());
    }

    private String buildRedirectUrl(SearchContext searchContext) {
        return redirectBuilder.buildRedirectUrl(
            searchContext.getSearchQuery(),
            searchContext.getDepartmentId(),
            searchContext.getPage());
    }
}
