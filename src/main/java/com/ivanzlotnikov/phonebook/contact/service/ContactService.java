package com.ivanzlotnikov.phonebook.contact.service;

import com.ivanzlotnikov.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phonebook.contact.mapper.ContactMapper;
import com.ivanzlotnikov.phonebook.contact.repository.ContactRepository;
import com.ivanzlotnikov.phonebook.department.entity.Department;
import com.ivanzlotnikov.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phonebook.exception.InvalidDataException;
import com.ivanzlotnikov.phonebook.exception.ResourceNotFoundException;
import com.ivanzlotnikov.phonebook.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления контактами в телефонном справочнике. Предоставляет бизнес-логику для
 * операций CRUD, поиска и фильтрации контактов. Все методы записи выполняются в транзакциях.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final DepartmentService departmentService;

    /**
     * Получает все контакты с пагинацией.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница контактов в виде DTO
     */
    @Transactional(readOnly = true)
    public Page<ContactDTO> findAll(Pageable pageable) {
        log.info("Fetching all contacts for page {} with size {}", pageable.getPageNumber(),
            pageable.getPageSize());
        return contactRepository.findAllWithDepartment(pageable).map(contactMapper::toDto);
    }

    /**
     * Находит контакт по идентификатору.
     *
     * @param id идентификатор контакта
     * @return DTO контакта
     * @throws ResourceNotFoundException если контакт не найден
     */
    @Transactional(readOnly = true)
    public ContactDTO findById(Long id) {
        log.info("Finding contacts by id: {}", id);
        Contact contact = contactRepository.findByIdWithDepartment(id)
            .orElseThrow(() -> new ResourceNotFoundException("Контакт " + id));
        return contactMapper.toDto(contact);
    }

    /**
     * Сохраняет новый контакт или обновляет существующий.
     *
     * @param contactDTO данные контакта для сохранения
     * @return сохраненный контакт в виде DTO
     */
    @CacheEvict(cacheNames = "contactCount", allEntries = true)
    public ContactDTO save(ContactFormDTO contactDTO) {
        log.info("Saving contact with id: {}", contactDTO.getId());
        Contact contact = contactMapper.toEntity(contactDTO);

        if (contactDTO.getDepartmentId() != null) {
            Department department = departmentService.findEntityById(contactDTO.getDepartmentId());
            contact.setDepartment(department);
        }

        Contact savedContact = contactRepository.save(contact);
        return contactMapper.toDto(savedContact);
    }

    /**
     * Удаляет контакт по идентификатору.
     *
     * @param id идентификатор контакта для удаления
     * @throws ResourceNotFoundException если контакт не найден
     */
    @CacheEvict(cacheNames = "contactCount", allEntries = true)
    @Async
    public void deleteById(long id) {
        if (!contactRepository.existsById(id)) {
            throw new ResourceNotFoundException("Контакт " + id);
        }
        contactRepository.deleteById(id);
    }

    /**
     * Удаляет несколько контактов по списку идентификаторов.
     *
     * @param ids список идентификаторов контактов для удаления
     */
    @CacheEvict(cacheNames = "contactCount", allEntries = true)
    @Async
    public void deleteAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw InvalidDataException.forField("contactIds", "Список контактов для удаления пуст");
        }
        contactRepository.deleteAllByIdIn(ids);
        log.info("Successfully deleted contacts with IDs: {}", ids);
    }

    /**
     * Выполняет поиск контактов по имени с пагинацией.
     *
     * @param name     часть имени для поиска
     * @param pageable параметры пагинации
     * @return страница найденных контактов
     */
    @Transactional(readOnly = true)
    public Page<ContactDTO> searchByName(String name, Pageable pageable) {
        String normalizedName = StringUtils.trimSafely(name);
        log.info("Searching contacts by name: {}", normalizedName);
        return contactRepository.findByFullNameContainingIgnoreCase(normalizedName, pageable)
            .map(contactMapper::toDto);
    }

    /**
     * Находит контакты в указанном департаменте и всех его поддепартаментах.
     *
     * @param departmentId идентификатор департамента
     * @param pageable     параметры пагинации
     * @return страница контактов из департамента и его поддепартаментов
     */
    @Transactional(readOnly = true)
    public Page<ContactDTO> findByDepartmentHierarchy(Long departmentId, Pageable pageable) {
        List<Long> departmentIds = getDepartmentIdsWithHierarchy(departmentId);
        return contactRepository.findByDepartmentIdInWithDepartment(departmentIds, pageable)
            .map(contactMapper::toDto);
    }

    /**
     * Выполняет комбинированный поиск контактов по имени и департаменту. Ищет в указанном
     * департаменте и всех его поддепартаментах.
     *
     * @param name         часть имени для поиска
     * @param departmentId идентификатор департамента для фильтрации
     * @param pageable     параметры пагинации
     * @return страница найденных контактов
     */
    @Transactional(readOnly = true)
    public Page<ContactDTO> searchByNameAndDepartment(String name, Long departmentId,
        Pageable pageable) {
        String normalizedName = StringUtils.trimSafely(name);
        log.info("Searching contacts by name: {} and department: {}", normalizedName, departmentId);
        List<Long> departmentIds = getDepartmentIdsWithHierarchy(departmentId);
        return contactRepository.findByNameAndDepartmentIds(normalizedName, departmentIds, pageable)
            .map(contactMapper::toDto);
    }

    private List<Long> getDepartmentIdsWithHierarchy(Long departmentId) {
        List<Department> departments = departmentService.getDepartmentsHierarchy(departmentId);
        List<Long> departmentIds = departments.stream()
            .map(Department::getId)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        departmentIds.add(departmentId);
        return departmentIds;
    }

    /**
     * Проверяет существование контакта с указанными ФИО и должностью. Используется для
     * предотвращения дублирования контактов.
     *
     * @param fullName полное имя сотрудника
     * @param position должность сотрудника
     * @return true, если контакт с такими данными уже существует
     */
    @Transactional(readOnly = true)
    public boolean existsByFullNameAndPosition(String fullName, String position) {
        return contactRepository.existsByFullNameAndPosition(
            StringUtils.trimSafely(fullName),
            StringUtils.trimSafely(position));
    }

    /**
     * Подсчитывает общее количество контактов в системе.
     *
     * @return общее количество контактов
     */
    @Cacheable("contactCount")
    @Transactional(readOnly = true)
    public long count() {
        return contactRepository.count();
    }
}
