package com.ivanzlotnikov.phone_book.phonebook.contact.repository;

import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    //    @Query("SELECT COUNT(c) FROM Contact c WHERE c.department.id =:departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    //Пагинация общий список
    Page<Contact> findAll(Pageable pageable);

    //Поиск по имени
    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE LOWER(c.fullName) LIKE LOWER(concat('%', :name, '%'))")
    Page<Contact> findByFullNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    Page<Contact> findByDepartmentId(Long departmentId, Pageable pageable);
    boolean existsByFullNameAndPosition(String fullName, String position);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE c.id = :id")
    Optional<Contact> findByIdWithDepartment(@Param("id") Long id);

    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department",
        countQuery = "SELECT count(c) FROM Contact c")
    Page<Contact> findAllWithDepartment(Pageable pageable);

    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department d WHERE d.id IN :departmentIds",
        countQuery = "SELECT count(c) FROM Contact c WHERE c.department.id IN :departmentIds")
    Page<Contact> findByDepartmentIdInWithDepartment(@Param("departmentIds") List<Long> departmentIds, Pageable pageable);

    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department d WHERE LOWER(c.fullName) LIKE LOWER(concat('%', :name, '%')) AND d.id IN :departmentIds",
        countQuery = "SELECT count(c) FROM Contact c WHERE LOWER(c.fullName) LIKE LOWER(concat('%', :name, '%')) AND c.department.id IN :departmentIds")
    Page<Contact> findByNameAndDepartmentIds(@Param("name") String name, @Param("departmentIds") List<Long> departmentIds, Pageable pageable);

}
