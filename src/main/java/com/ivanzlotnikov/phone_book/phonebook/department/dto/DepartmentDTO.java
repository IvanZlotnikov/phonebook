package com.ivanzlotnikov.phone_book.phonebook.department.dto;

import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static DepartmentDTO fromEntity(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setContactCount(department.getContacts() != null ? department.getContacts().size() : 0);

        if (department.getParentDepartment() != null) {
            dto.setParentDepartmentId(department.getParentDepartment().getId());
            dto.setParentDepartmentName(department.getParentDepartment().getName());
        }
        return dto;
    }

}
