package com.ivanzlotnikov.phone_book.phonebook.department.dto;

import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;

public record DepartmentWithContactCountDTO(Department department, long contactCount) {

}
