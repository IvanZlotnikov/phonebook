package com.ivanzlotnikov.phone_book.phonebook.department.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления департамента.
 * Содержит информацию о департаменте, его родителе, количестве контактов и дочерних департаментах.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

    private Long id;
    private String name;
    private Long parentDepartmentId;
    private String parentDepartmentName;
    private int contactCount;
    private List<DepartmentDTO> childrenDepartments = new ArrayList<>();
}
