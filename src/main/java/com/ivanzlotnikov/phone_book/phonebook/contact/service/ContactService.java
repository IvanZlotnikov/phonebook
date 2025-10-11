package com.ivanzlotnikov.phone_book.phonebook.contact.service;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phone_book.phonebook.contact.repository.ContactRepository;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import com.ivanzlotnikov.phone_book.phonebook.department.repository.DepartmentRepository;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;

    @Transactional
    public void deleteAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("Attempt to delete contacts with empty or null ID list");
            return;
        }
        List<Long> existingIds = ids.stream()
            .filter(contactRepository::existsById)
            .toList();

        if (existingIds.isEmpty()) {
            log.info("No existing contacts found for deletion with IDs: {}", ids);
            return;
        }

        contactRepository.deleteAllByIdIn(existingIds);
        log.info("Successfully deleted {} contacts with IDs: {}", existingIds.size(), existingIds);

    }

    @Transactional(readOnly = true)
    public List<ContactDTO> findAll() {
        return contactRepository.findAll()
            .stream()
            .map(ContactDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ContactDTO> findById(long id) {
        return contactRepository.findById(id)
            .map(ContactDTO::fromEntity);
    }

    public ContactDTO save(ContactDTO contactDTO) {
        Contact contact;

        if (contactDTO.getId() != null) {
            contact = contactRepository.findById(contactDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Contact not found with id: " + contactDTO.getId()));
        } else {
            contact = new Contact();
        }

        // Обновляем поля
        contact.setFullName(contactDTO.getFullName().trim());
        contact.setPosition(contactDTO.getPosition().trim());

        // Устанавливаем подразделение
        if (contactDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(contactDTO.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Department not found with id: " + contactDTO.getDepartmentId()));
            contact.setDepartment(department);
        } else {
            contact.setDepartment(null);
        }

        // Очищаем и обновляем телефоны
        contact.getWorkPhones().clear();
        contact.getWorkPhones().addAll(contactDTO.getWorkPhones());

        contact.getWorkMobilePhones().clear();
        contact.getWorkMobilePhones()
            .addAll(contactDTO.getWorkMobilePhones());

        contact.getPersonalPhones().clear();
        contact.getPersonalPhones().addAll(contactDTO.getPersonalPhones());

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact saved: {} successfully", savedContact.getId());

        return ContactDTO.fromEntity(savedContact);
    }

    public void deleteById(long id) {
        if (!contactRepository.existsById(id)) {
            throw new EntityNotFoundException("Contact not found with id: " + id);
        }
        contactRepository.deleteById(id);
        log.info("Contact deleted: {} successfully", id);
    }

    @Transactional(readOnly = true)
    public List<ContactDTO> searchByName(String name) {
        return contactRepository.findByFullNameContainingIgnoreCase(
                name.trim()).stream()
            .map(ContactDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContactDTO> findByDepartmentHierarchy(Long departmentId) {
        // 1. Найти целевой отдел
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("Department not found with id: " + departmentId));

        List<Department> allDepartments =new ArrayList<>(departmentService.getDepartmentsHierarchy(departmentId));

        allDepartments.add(department);

        return contactRepository.findByDepartmentIn(allDepartments).stream()
            .map(ContactDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByFullNameAndPosition(String fullName, String position) {
        return contactRepository.findByFullNameAndPosition(fullName.trim(), position.trim())
            .isPresent();
    }

    @Transactional(readOnly = true)
    public long count() {
        return contactRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByDepartment(Long departmentId) {
        return contactRepository.countByDepartmentId(departmentId);
    }


}
