package com.ivanzlotnikov.phonebook.department.service;

import com.ivanzlotnikov.phonebook.contact.repository.ContactRepository;
import com.ivanzlotnikov.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phonebook.department.dto.DepartmentWithContactCountDTO;
import com.ivanzlotnikov.phonebook.department.entity.Department;
import com.ivanzlotnikov.phonebook.department.mapper.DepartmentMapper;
import com.ivanzlotnikov.phonebook.department.repository.DepartmentRepository;
import com.ivanzlotnikov.phonebook.exception.InvalidDataException;
import com.ivanzlotnikov.phonebook.exception.ResourceNotFoundException;
import com.ivanzlotnikov.phonebook.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления департаментами организации. Обрабатывает иерархическую структуру
 * департаментов, включая операции с родительскими и дочерними подразделениями.
 */
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

    /**
     * Получает все департаменты с количеством контактов.
     *
     * @return список всех департаментов с подсчетом контактов
     */
    public List<DepartmentDTO> findAll() {
        return departmentRepository.findAllWithContactCount().stream()
            .map(this::mapWithContactCount)
            .toList();
    }

    /**
     * Находит департамент по идентификатору.
     *
     * @param id идентификатор департамента
     * @return Optional с DTO департамента или пустой Optional
     */
    @Transactional(readOnly = true)
    public Optional<DepartmentDTO> findById(Long id) {
        return departmentRepository.findById(id)
            .map(departmentMapper::toDto);
    }

    /**
     * Находит сущность департамента по идентификатору. Используется внутри сервисов для получения
     * entity объекта.
     *
     * @param id идентификатор департамента
     * @return сущность департамента
     * @throws ResourceNotFoundException если департамент не найден
     */
    @Transactional(readOnly = true)
    public Department findEntityById(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.byId("Департамент ", id));
    }

    /**
     * Сохраняет новый департамент или обновляет существующий.
     *
     * @param departmentDTO данные департамента
     * @return сохраненный департамент в виде DTO
     * @throws ResourceNotFoundException если департамент не найден
     */
    public DepartmentDTO save(DepartmentDTO departmentDTO) {
        Department department;
        if (departmentDTO.getId() != null) {
            department = departmentRepository.findById(departmentDTO.getId())
                .orElseThrow(
                    () -> ResourceNotFoundException.byId("Департамент", departmentDTO.getId()));
        } else {
            department = new Department();
        }
        department.setName(StringUtils.trimSafely(departmentDTO.getName()));

        if (departmentDTO.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findById(
                    departmentDTO.getParentDepartmentId())
                .orElseThrow(() -> ResourceNotFoundException.byId(
                    "Родительский департамент", departmentDTO.getParentDepartmentId()));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }
        Department savedDepartment = departmentRepository.save(department);
        log.info("Department {} saved successfully", savedDepartment.getId());

        return departmentMapper.toDto(savedDepartment);
    }

    /**
     * Удаляет департамент по идентификатору. Проверяет наличие контактов и дочерних департаментов
     * перед удалением.
     *
     * @param id идентификатор департамента
     * @throws ResourceNotFoundException если департамент содержит контакты или поддепартаменты
     */
    public void deleteById(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.byId("Департамент", id));

        if (!department.getChildrenDepartments().isEmpty()) {
            throw InvalidDataException.forField("id",
                "Нельзя удалить департамент с дочерними подразделениями");
        }

        long contactCount = contactRepository.countByDepartmentId(id);
        if (contactCount > 0) {
            throw InvalidDataException.forField("id",
                "Нельзя удалить департамент, содержащий контакты");
        }

        departmentRepository.deleteById(id);
        log.info("Department {} deleted successfully", id);
    }

    /**
     * Получает все корневые департаменты (без родительского департамента).
     *
     * @return список корневых департаментов с количеством контактов
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> findRootDepartments() {
        return departmentRepository.findRootDepartmentsWithContactCount().stream()
            .map(this::mapWithContactCount)
            .toList();
    }

    /**
     * Находит прямых потомков (дочерние департаменты первого уровня) указанного департамента.
     *
     * @param parentId идентификатор родительского департамента
     * @return список дочерних департаментов
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> findDirectChildren(Long parentId) {
        return departmentRepository.findByParentDepartmentId(parentId).stream()
            .map(departmentMapper::toDto)
            .toList();
    }

    /**
     * Получает все поддепартаменты указанного департамента на всех уровнях вложенности. Использует
     * рекурсивный обход иерархии с ограничением глубины.
     *
     * @param departmentId идентификатор родительского департамента
     * @return список всех поддепартаментов
     */
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsHierarchy(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> ResourceNotFoundException.byId("Департамент", departmentId));

        List<Department> allDepartments = departmentRepository.findAllWithParent();

        Map<Long, List<Department>> childrenMap = allDepartments.stream()
            .filter(d -> d.getParentDepartment() != null)
            .collect(Collectors.groupingBy(d -> d.getParentDepartment().getId()));

        List<Department> result = new ArrayList<>();
        collectChildrenFromMap(department.getId(), childrenMap, result, MAX_HIERARCHY_DEPTH);
        return result;
    }

    private void collectChildrenFromMap(Long parentId,
        Map<Long, List<Department>> childrenMap,
        List<Department> result,
        int maxDepth) {
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

    /**
     * Строит полное иерархическое дерево департаментов. Возвращает корневые департаменты со всеми
     * вложенными поддепартаментами.
     *
     * @return список корневых департаментов с вложенной иерархией
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartmentTree() {
        List<Department> allDepartments = departmentRepository.findAllWithParent();

        Map<Long, List<Department>> childrenMap = allDepartments.stream()
            .filter(d -> d.getParentDepartment() != null)
            .collect(Collectors.groupingBy(d -> d.getParentDepartment().getId()));

        List<Department> rootDepartments = allDepartments.stream()
            .filter(d -> d.getParentDepartment() == null)
            .toList();

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

    /**
     * Выполняет поиск департаментов по названию с подсчетом контактов.
     *
     * @param name часть названия для поиска
     * @return список найденных департаментов с количеством контактов
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> searchByName(String name) {
        return departmentRepository.findByNameWithContactCount(StringUtils.trimSafely(name))
            .stream()
            .map(this::mapWithContactCount)
            .toList();
    }

    /**
     * Проверяет существование департамента с указанным названием. Используется для предотвращения
     * дублирования названий.
     *
     * @param name название департамента
     * @return true, если департамент с таким названием существует
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return departmentRepository.existsByName(StringUtils.trimSafely(name));
    }

    /**
     * Подсчитывает общее количество департаментов в системе.
     *
     * @return общее количество департаментов
     */
    @Transactional(readOnly = true)
    public long count() {
        return departmentRepository.count();
    }

    /**
     * Получает все департаменты для использования в формах (без дополнительных данных).
     * Оптимизированный метод для заполнения выпадающих списков.
     *
     * @return список всех департаментов
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> findAllForForms() {
        return departmentRepository.findAll().stream()
            .map(departmentMapper::toDto)
            .toList();
    }

    private DepartmentDTO mapWithContactCount(DepartmentWithContactCountDTO agg) {
        DepartmentDTO dto = departmentMapper.toDto(agg.department());
        dto.setContactCount((int) agg.contactCount());
        return dto;
    }
}

