package com.ivanzlotnikov.phone_book.phonebook.department.dto;

import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;

public class DepartmentWithContactCountDTO {

    private Department department;
    private long contactCount;

    public DepartmentWithContactCountDTO(Department department, long contactCount) {
        this.department = department;
        this.contactCount = contactCount;
    }

    public Department getDepartment() {
        return department;
    }

    public long getContactCount() {
        return contactCount;
    }

}
