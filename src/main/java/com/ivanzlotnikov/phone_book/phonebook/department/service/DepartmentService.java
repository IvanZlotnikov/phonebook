package com.ivanzlotnikov.phone_book.phonebook.department.service;

import com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import com.ivanzlotnikov.phone_book.phonebook.contact.repository.ContactRepository;
import com.ivanzlotnikov.phone_book.phonebook.department.repository.DepartmentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ContactRepository contactRepository;

    // Получить все подразделения
    @Transactional(readOnly = true)
    public List<DepartmentDTO> findAll() {
        return departmentRepository.findAll().stream()
            .map(DepartmentDTO::fromEntity)
            .toList();
    }

    // Получить подразделение по ID
    @Transactional(readOnly = true)
    public Optional<DepartmentDTO> findById(Long id) {
        return departmentRepository.findById(id)
            .map(DepartmentDTO::fromEntity);
    }

    // Сохранить подразделение
    public DepartmentDTO save(DepartmentDTO departmentDTO) {
        Department department;
        if (departmentDTO.getId() != null) {
            department = departmentRepository.findById(departmentDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Department with id " + departmentDTO.getId() + " not found"));
        } else {
            department = new Department();
        }

        department.setName(departmentDTO.getName().trim());
        if (departmentDTO.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(
                    departmentDTO.getParentDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Parent department with id " + departmentDTO.getParentDepartmentId()
                    + " not found"));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }

        Department savedDepartment = departmentRepository.save(department);
        log.info("Department {} saved successfully", savedDepartment.getId());

        return DepartmentDTO.fromEntity(savedDepartment);
    }

    // Удалить подразделение
    public void deleteById(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        if (!department.getChildrenDepartments().isEmpty()) {
            throw new IllegalStateException("Cannot delete department with child departments");
        }

        long contactCount = contactRepository.countByDepartmentId(id);
        if (contactCount > 0) {
            throw new IllegalStateException("Cannot delete department with contacts");
        }

        departmentRepository.deleteById(id);
        log.info("Department {} deleted successfully", id);
    }

    // Получить все корневые подразделения
    @Transactional(readOnly = true)
    public List<DepartmentDTO> findRootDepartments() {
        return departmentRepository.findByParentDepartmentIsNull().stream()
            .map(DepartmentDTO::fromEntity)
            .toList();
    }


    // Получить прямых потомков подразделения
    @Transactional(readOnly = true)
    public List<DepartmentDTO> findDirectChildren(Long parentId) {
        return departmentRepository.findByParentDepartmentId(parentId).stream()
            .map(DepartmentDTO::fromEntity)
            .toList();
    }

    // Получить все подразделения в иерархии (рекурсивно)
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsHierarchy(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        List<Department> allDepartments = new ArrayList<>();
        addChildrenToHierarchy(department, allDepartments);
        return allDepartments;
    }


    // Рекурсивный метод для построения иерархии
    private void addChildrenToHierarchy(Department parent, List<Department> hierarchy) {
        List<Department> children = departmentRepository.findByParentDepartment(parent);
        for (Department child : children) {
            hierarchy.add(child);
            addChildrenToHierarchy(child, hierarchy);
        }
    }

    // Получить все подразделения в виде дерева
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartmentTree() {
        return departmentRepository.findByParentDepartmentIsNull()
            .stream()
            .map(dept->buildDepartmentTree(dept,3))
            .toList();
    }

    private DepartmentDTO buildDepartmentTree(Department department,int maxDepth) {
        DepartmentDTO dto = DepartmentDTO.fromEntity(department);

        if (maxDepth > 0) {
            dto.setChildrenDepartments(department.getChildrenDepartments().stream()
                .map(child->buildDepartmentTree(child, maxDepth-1))
                .collect(Collectors.toList()));
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> searchByName(String name) {
        return departmentRepository.findByNameContainingIgnoreCase(name.trim())
            .stream()
            .map(DepartmentDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return departmentRepository.existsByName(name.trim());
    }

    @Transactional(readOnly = true)
    public long count() {
        return departmentRepository.count();
    }

    @Transactional(readOnly = true)
    public long countContactsByDepartment(Long departmentId) {
        return contactRepository.countByDepartmentId(departmentId);
    }


}

