package com.ivanzlotnikov.phone_book.phonebook.department.repository;

import com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentWithContactCountDTO;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Корневые подразделения
    List<Department> findByParentDepartmentIsNull();

    // Найти прямых потомков определенного подразделения
    List<Department> findByParentDepartmentId(Long parentId);

    // Найти подразделение по имени
    Optional<Department> findByName(String name);

    //Проверить существование подразделения по имени
    boolean existsByName(String name);

    //Найти все подразделения с определенным родителем
    List<Department> findByParentDepartment(Department parent);

    //Рекурсивный поиск всех потомков(для postgresql с СТЕ)
    @Query(value = """
            WITH RECURSIVE sub_departments AS (
                SELECT id, name, parent_department_id
                FROM departments
                WHERE id = :departmentId
                UNION ALL
                SELECT d.id, d.name, d.parent_department_id
                FROM departments d
                INNER JOIN sub_departments sd ON d.parent_department_id = sd.id
            )
            SELECT * FROM departments WHERE id IN (SELECT id FROM sub_departments WHERE id != :departmentId)
        """, nativeQuery = true)
    List<Department> findAllSubDepartments(@Param("departmentId") Long departmentId);

    //Поиск по имени без учета регистра
    List<Department> findByNameContainingIgnoreCase(String name);

    // Список подразделений с количеством контактов
    @Query("SELECT new com.ivanzlotnikov.phone_book.phonebook.department.dto.DepartmentWithContactCountDTO(d,COUNT(c.id)) " +
           "FROM Department d LEFT JOIN d.contacts c GROUP BY d.id")
    List<DepartmentWithContactCountDTO> findAllWithContactCount();
}
