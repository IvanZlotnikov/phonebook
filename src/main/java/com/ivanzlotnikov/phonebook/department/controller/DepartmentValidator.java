package com.ivanzlotnikov.phonebook.department.controller;

import com.ivanzlotnikov.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phonebook.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Валидатор для проверки департаментов на дубликаты.
 * Проверяет уникальность названия при создании нового департамента.
 */
@Component
@RequiredArgsConstructor
public class DepartmentValidator {

    private final DepartmentService departmentService;

    /**
     * Проверяет, существует ли департамент с таким названием.
     * Применяется только при создании (если ID == null).
     *
     * @param departmentDTO данные департамента
     * @throws DuplicateResourceException если название уже занято
     */
    public void checkForDuplicate(DepartmentDTO departmentDTO) {
        if (departmentDTO.getId() == null &&
            departmentService.existsByName(departmentDTO.getName())) {
            throw DuplicateResourceException.of(
                "Департамент", "название", departmentDTO.getName());
        }
    }
}