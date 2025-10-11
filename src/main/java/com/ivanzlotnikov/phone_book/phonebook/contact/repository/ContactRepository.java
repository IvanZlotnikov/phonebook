package com.ivanzlotnikov.phone_book.phonebook.contact.repository;

import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    void deleteAllByIdIn(List<Long> ids);

    List<Contact> findByDepartment(Department department);

    List<Contact> findByFullNameContainingIgnoreCase(String name);

    List<Contact> findByFullNameContainingIgnoreCaseAndDepartment(String name,
        Department department);

    //    //Поиск по иерархии подразделений
    @Query("SELECT c FROM Contact c WHERE c.department IN :departments")
    List<Contact> findByDepartmentIn(@Param("departments") List<Department> departments);

    @Query("SELECT COUNT(c) FROM Contact c WHERE c.department.id =:departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    Optional<Contact> findByFullNameAndPosition(String fullName, String position);
}
