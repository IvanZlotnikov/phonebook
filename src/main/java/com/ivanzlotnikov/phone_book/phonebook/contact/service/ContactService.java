package com.ivanzlotnikov.phone_book.phonebook.contact.service;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phone_book.phonebook.contact.repository.ContactRepository;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import com.ivanzlotnikov.phone_book.phonebook.department.repository.DepartmentRepository;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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

    @Transactional(readOnly = true)
    public List<ContactDTO> findAll() {
        return contactRepository.findAllWithDepartment()
            .stream()
            .map(ContactDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ContactDTO> findById(long id) {
        return contactRepository.findByIdWithDepartmentAndPhones(id)
            .map(ContactDTO::fromEntity);
    }

    public ContactDTO save(ContactFormDTO contactFormDTO) {
        Contact contact;

        if (contactFormDTO.getId() != null) {
            contact = contactRepository.findByIdWithDepartmentAndPhones(contactFormDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Contact not found with id: " + contactFormDTO.getId()));
        } else {
            contact = new Contact();
        }
        // Обновляем поля
        contact.setFullName(contactFormDTO.getFullName().trim());
        contact.setPosition(contactFormDTO.getPosition().trim());

        Long newDepartmentId = contactFormDTO.getDepartmentId();
        Long currentDepartmentId =
            contact.getDepartment() != null ? contact.getDepartment().getId() : null;

        // Устанавливаем подразделение
        if (!Objects.equals(newDepartmentId, currentDepartmentId)) {
            if (newDepartmentId != null) {
                Department department = departmentRepository.findById(contactFormDTO.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Department not found with id: " + contactFormDTO.getDepartmentId()));
                contact.setDepartment(department);
            } else {
                contact.setDepartment(null);
            }
        }
        //Обновляем телефоны только если изменилось
        updatePhonesIfChanged(contact, contactFormDTO);

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact saved: {} successfully", savedContact.getId());

        return ContactDTO.fromEntity(savedContact);
    }

    private void updatePhonesIfChanged(Contact contact, ContactFormDTO contactFormDTO) {
        List<String> newWorkPhones = filterEmptyPhones(contactFormDTO.getWorkPhones());
        List<String> newWorkMobilePhones = filterEmptyPhones(contactFormDTO.getWorkMobilePhones());
        List<String> newPersonalPhones = filterEmptyPhones(contactFormDTO.getPersonalPhones());

        // Очищаем и обновляем телефоны
        if (!newWorkPhones.equals(contact.getWorkPhones())) {
            contact.getWorkPhones().clear();
            contact.getWorkPhones().addAll(contactFormDTO.getWorkPhones());
        }

        if (!newWorkMobilePhones.equals(contact.getWorkMobilePhones())) {
            contact.getWorkMobilePhones().clear();
            contact.getWorkMobilePhones().addAll(contactFormDTO.getWorkMobilePhones());
        }

        if (!newPersonalPhones.equals(contact.getPersonalPhones())) {
            contact.getPersonalPhones().clear();
            contact.getPersonalPhones().addAll(contactFormDTO.getPersonalPhones());
        }
    }

    private List<String> filterEmptyPhones(List<String> phones) {
        if (phones == null) {
            return List.of();
        }
        return phones.stream()
            .filter(phone -> phone != null && !phone.trim().isEmpty())
            .map(String::trim)
            .toList();
    }

    public void deleteById(long id) {
        try {
            contactRepository.deleteById(id);
            log.info("Contact deleted: {} successfully", id);

        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Contact not found with id: " + id);
        }
    }

    public void deleteAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("Attempt to delete contacts with empty or null ID list");
            return;
        }
        int deletedCount = contactRepository.deleteAllByIdIn(ids);
        log.info("Successfully deleted {} contacts with IDs: {}", deletedCount, ids);
    }

    @Transactional(readOnly = true)
    public List<ContactDTO> findByDepartmentHierarchy(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(
                () -> new EntityNotFoundException(
                    "Department not found with id: " + departmentId));

        List<Department> allDepartments = new ArrayList<>(
            departmentService.getDepartmentsHierarchy(departmentId));
        allDepartments.add(department);

        return contactRepository.findByDepartmentInWithDepartment(allDepartments)
            .stream()
            .map(ContactDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactDTO> searchByName(String name) {
        return contactRepository.findByFullNameContainingIgnoreCaseWithDepartment(name.trim())
            .stream()
            .map(ContactDTO::fromEntity)
            .collect(Collectors.toList());
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
