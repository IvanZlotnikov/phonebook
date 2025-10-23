package com.ivanzlotnikov.phone_book.phonebook.department.service;

import com.ivanzlotnikov.phone_book.phonebook.contact.repository.ContactRepository;
import com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentWithContactCountDTO;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import com.ivanzlotnikov.phone_book.phonebook.department.mapper.DepartmentMapper;
import com.ivanzlotnikov.phone_book.phonebook.department.repository.DepartmentRepository;
import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private static final int MAX_HIERARCHY_DEPTH = 10;
    private static final int MAX_TREE_DEPTH = 5;

    private final DepartmentRepository departmentRepository;
    private final ContactRepository contactRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional(readOnly = true)
    public List<DepartmentDTO> findAll() {
        return departmentRepository.findAllWithContactCount().stream()
            .map((DepartmentWithContactCountDTO agg) -> {
                DepartmentDTO dto = departmentMapper.toDto(agg.department());
                dto.setContactCount((int) agg.contactCount());
                return dto;
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DepartmentDTO> findById(Long id) {
        return departmentRepository.findById(id)
            .map(departmentMapper::toDto);
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

        return departmentMapper.toDto(savedDepartment);
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

    @Transactional(readOnly = true)
    public List<DepartmentDTO> findRootDepartments() {
        // Решение N+1: используем JOIN вместо отдельных запросов
        return departmentRepository.findRootDepartmentsWithContactCount().stream()
            .map(agg -> {
                DepartmentDTO dto = departmentMapper.toDto(agg.department());
                dto.setContactCount((int) agg.contactCount());
                return dto;
            })
            .toList();
    }

    private DepartmentDTO toDtoWithContactCount(Department department) {
        DepartmentDTO dto = departmentMapper.toDto(department);
        dto.setContactCount((int) contactRepository.countByDepartmentId(department.getId()));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> findDirectChildren(Long parentId) {
        return departmentRepository.findByParentDepartmentId(parentId).stream()
            .map(departmentMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartmentsHierarchy(Long departmentId) {
        // Решение N+1: загружаем все департаменты одним запросом и строим иерархию в памяти
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        // Загружаем все департаменты одним запросом
        List<Department> allDepartments = departmentRepository.findAllWithParent();
        
        // Создаем Map для быстрого поиска детей по parent_id
        Map<Long, List<Department>> childrenMap = allDepartments.stream()
            .filter(d -> d.getParentDepartment() != null)
            .collect(Collectors.groupingBy(d -> d.getParentDepartment().getId()));

        // Собираем иерархию в памяти
        List<Department> result = new ArrayList<>();
        collectChildrenFromMap(department.getId(), childrenMap, result, MAX_HIERARCHY_DEPTH);
        return result;
    }

    private void collectChildrenFromMap(Long parentId, Map<Long, List<Department>> childrenMap, 
                                        List<Department> result, int maxDepth) {
        if (maxDepth <= 0) {
            log.warn("Max depth reached for department hierarchy starting from {}", parentId);
            return;
        }

        List<Department> children = childrenMap.getOrDefault(parentId, List.of());
        for (Department child : children) {
            result.add(child);
            collectChildrenFromMap(child.getId(), childrenMap, result, maxDepth - 1);
        }
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartmentTree() {
        // Решение N+1: загружаем все департаменты одним запросом
        List<Department> allDepartments = departmentRepository.findAllWithParent();
        
        // Создаем Map для быстрого поиска детей
        Map<Long, List<Department>> childrenMap = allDepartments.stream()
            .filter(d -> d.getParentDepartment() != null)
            .collect(Collectors.groupingBy(d -> d.getParentDepartment().getId()));
        
        // Находим корневые департаменты
        List<Department> rootDepartments = allDepartments.stream()
            .filter(d -> d.getParentDepartment() == null)
            .toList();
        
        // Строим дерево для каждого корневого департамента
        return rootDepartments.stream()
            .map(dept -> buildDepartmentTreeFromMap(dept, childrenMap, MAX_TREE_DEPTH))
            .toList();
    }

    private DepartmentDTO buildDepartmentTreeFromMap(Department department, 
                                                      Map<Long, List<Department>> childrenMap, 
                                                      int maxDepth) {
        DepartmentDTO dto = departmentMapper.toDto(department);
        if (maxDepth > 0) {
            List<Department> children = childrenMap.getOrDefault(department.getId(), List.of());
            dto.setChildrenDepartments(children.stream()
                .map(child -> buildDepartmentTreeFromMap(child, childrenMap, maxDepth - 1))
                .toList());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> searchByName(String name) {
        // Решение N+1: используем JOIN вместо отдельных запросов
        return departmentRepository.findByNameWithContactCount(name.trim()).stream()
            .map(agg -> {
                DepartmentDTO dto = departmentMapper.toDto(agg.department());
                dto.setContactCount((int) agg.contactCount());
                return dto;
            })
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
    public List<DepartmentDTO> findAllForForms() {
        return departmentRepository.findAll().stream()
            .map(departmentMapper::toDto)
            .toList();
    }
}

