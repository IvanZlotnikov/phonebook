package com.ivanzlotnikov.phone_book.phonebook.contact.repository;

import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import io.micrometer.observation.ObservationFilter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Modifying
    @Query("DELETE FROM Contact c WHERE c.id IN :ids")
    int deleteAllByIdIn(@Param("ids") List<Long> ids);

    List<Contact> findByDepartment(Department department);

    List<Contact> findByFullNameContainingIgnoreCase(String name);

    List<Contact> findByFullNameContainingIgnoreCaseAndDepartment(String name,
        Department department);

    //    //Поиск по иерархии подразделений
//    @Query("SELECT c FROM Contact c WHERE c.department IN :departments")
    List<Contact> findByDepartmentIn(@Param("departments") List<Department> departments);

    //    @Query("SELECT COUNT(c) FROM Contact c WHERE c.department.id =:departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    Optional<Contact> findByFullNameAndPosition(String fullName, String position);

    @Query("SELECT DISTINCT c FROM Contact c LEFT JOIN FETCH c.department ORDER BY c.fullName")
    List<Contact> findAllWithDepartment();

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE c.id = :id")
    Optional<Contact> findByIdWithDepartment(@Param("id") Long id);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE c.id = :id")
    Optional<Contact> findByIdWithPhones(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Contact c LEFT JOIN FETCH c.department WHERE c.department IN :departments")
    List<Contact> findByDepartmentInWithDepartment(
        @Param("departments") List<Department> departments);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE LOWER(c.fullName) LIKE LOWER(concat('%', :name, '%'))")
    List<Contact> findByFullNameContainingIgnoreCaseWithDepartment(@Param("name") String name);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE c.id = :id")
    Optional<Contact> findByIdWithDepartmentAndPhones(@Param("id") Long id);


}
