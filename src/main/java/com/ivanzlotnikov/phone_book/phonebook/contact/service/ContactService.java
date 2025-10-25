package com.ivanzlotnikov.phone_book.phonebook.contact.service;

import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phone_book.phonebook.contact.mapper.ContactMapper;
import com.ivanzlotnikov.phone_book.phonebook.contact.repository.ContactRepository;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final DepartmentService departmentService;

    @Transactional(readOnly = true)
    public Page<ContactDTO> findAll(Pageable pageable) {
        log.info("Fetching all contacts for page {} with size {}", pageable.getPageNumber(),
            pageable.getPageSize());
        Page<Contact> contacts = contactRepository.findAllWithDepartment(pageable);
        return contacts.map(contactMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ContactDTO findById(Long id) {
        log.info("Finding contacts by id: {}", id);
        Contact contact = contactRepository.findByIdWithDepartment(id)
            .orElseThrow(() -> new EntityNotFoundException("Contact not found with id: " + id));
        return contactMapper.toDto(contact);
    }

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

    public void deleteById(long id) {
        if (!contactRepository.existsById(id)) {
            throw new EntityNotFoundException("Contact not found with id: " + id);
        }
        contactRepository.deleteById(id);
    }

    public void deleteAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("Attempt to delete contacts with empty or null ID list");
            return;
        }
        contactRepository.deleteAllByIdIn(ids);
        log.info("Successfully deleted contacts with IDs: {}", ids);
    }


    @Transactional(readOnly = true)
    public Page<ContactDTO> searchByName(String name, Pageable pageable) {
        log.info("Searching contacts by name: {}", name);
        return contactRepository.findByFullNameContainingIgnoreCase(name.trim(), pageable)
            .map(contactMapper::toDto);
    }


    @Transactional(readOnly = true)
    public Page<ContactDTO> findByDepartmentHierarchy(Long departmentId, Pageable pageable) {
        List<Long> departmentIds = getDepartmentIdsWithHierarchy(departmentId);
        return contactRepository.findByDepartmentIdInWithDepartment(departmentIds, pageable)
            .map(contactMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ContactDTO> searchByNameAndDepartment(String name, Long departmentId, Pageable pageable) {
        log.info("Searching contacts by name: {} and department: {}", name, departmentId);
        List<Long> departmentIds = getDepartmentIdsWithHierarchy(departmentId);
        return contactRepository.findByNameAndDepartmentIds(name.trim(), departmentIds, pageable)
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

    @Transactional(readOnly = true)
    public boolean existsByFullNameAndPosition(String fullName, String position) {
        return contactRepository.existsByFullNameAndPosition(fullName.trim(), position.trim());
    }

    @Transactional(readOnly = true)
    public long count() {
        return contactRepository.count();
    }

}
