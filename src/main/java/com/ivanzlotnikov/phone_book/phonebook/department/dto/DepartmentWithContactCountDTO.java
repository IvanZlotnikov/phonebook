package com.ivanzlotnikov.phone_book.phonebook.department.dto;

import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;

/**
 * DTO для представления департамента с подсчетом контактов.
 * Используется в JPQL запросах для агрегации данных.
 *
 * @param department сущность департамента
 * @param contactCount количество контактов в департаменте
 */
public record DepartmentWithContactCountDTO(Department department, long contactCount) {

}
