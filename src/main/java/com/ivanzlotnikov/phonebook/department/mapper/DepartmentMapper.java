package com.ivanzlotnikov.phonebook.department.mapper;

import com.ivanzlotnikov.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phonebook.department.entity.Department;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования сущности Department в DTO.
 * Выполняет конвертацию между слоями приложения.
 */
@Component
public class DepartmentMapper {

    /**
     * Преобразует сущность Department в DTO.
     *
     * @param department сущность департамента
     * @return DTO департамента или null
     */
    public DepartmentDTO toDto(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());

        if (department.getParentDepartment() != null) {
            dto.setParentDepartmentId(department.getParentDepartment().getId());
            dto.setParentDepartmentName(department.getParentDepartment().getName());
        }
        return dto;
    }
}
