package com.ivanzlotnikov.phonebook.contact.repository;

import com.ivanzlotnikov.phonebook.contact.entity.Contact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью {@link Contact}.
 * Предоставляет методы для выполнения операций CRUD и пользовательских запросов
 * к базе данных контактов с оптимизацией загрузки связанных сущностей.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Удаляет контакты по списку идентификаторов.
     *
     * @param ids список идентификаторов контактов для удаления
     * @return количество удаленных записей
     */
    @Modifying
    @Query("DELETE FROM Contact c WHERE c.id IN :ids")
    int deleteAllByIdIn(@Param("ids") List<Long> ids);

    /**
     * Подсчитывает количество контактов в указанном департаменте.
     *
     * @param departmentId идентификатор департамента
     * @return количество контактов в департаменте
     */
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * Выполняет поиск контактов по части фамилии, имени или отчества (без учета регистра).
     * Загружает департамент вместе с контактом для избежания N+1 проблемы.
     * Поиск выполняется по всем трем полям ФИО.
     * EntityGraph загружает все коллекции телефонов одним запросом.
     *
     * @param query поисковый запрос
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных контактов с департаментами и телефонами
     */
    @EntityGraph(attributePaths = {"workPhones", "workMobilePhones", "personalPhones"})
    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE " +
           "LOWER(c.lastName) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(c.firstName) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(c.middleName) LIKE LOWER(concat('%', :query, '%'))")
    Page<Contact> findByNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    /**
     * Проверяет существование контакта с указанными ФИО и должностью.
     *
     * @param lastName фамилия сотрудника
     * @param firstName имя сотрудника
     * @param middleName отчество сотрудника
     * @param position должность сотрудника
     * @return true, если контакт существует, иначе false
     */
    boolean existsByLastNameAndFirstNameAndMiddleNameAndPosition(String lastName, String firstName, String middleName, String position);

    /**
     * Находит контакт по идентификатору с загрузкой департамента и всех телефонов.
     *
     * @param id идентификатор контакта
     * @return Optional с контактом, департаментом и телефонами, или пустой Optional
     */
    @EntityGraph(attributePaths = {"workPhones", "workMobilePhones", "personalPhones"})
    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE c.id = :id")
    Optional<Contact> findByIdWithDepartment(@Param("id") Long id);

    /**
     * Получает все контакты с загрузкой департаментов и всех телефонов.
     * Использует отдельный запрос для подсчета общего количества записей.
     * EntityGraph оптимизирует загрузку, избегая N+1 проблемы.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница контактов с департаментами и телефонами
     */
    @EntityGraph(attributePaths = {"workPhones", "workMobilePhones", "personalPhones"})
    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department",
        countQuery = "SELECT count(c) FROM Contact c")
    Page<Contact> findAllWithDepartment(Pageable pageable);

    /**
     * Находит контакты по списку идентификаторов департаментов.
     * Загружает департаменты и все телефоны вместе с контактами.
     *
     * @param departmentIds список идентификаторов департаментов
     * @param pageable параметры пагинации и сортировки
     * @return страница контактов из указанных департаментов с телефонами
     */
    @EntityGraph(attributePaths = {"workPhones", "workMobilePhones", "personalPhones"})
    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department d WHERE d.id IN :departmentIds",
        countQuery = "SELECT count(c) FROM Contact c WHERE c.department.id IN :departmentIds")
    Page<Contact> findByDepartmentIdInWithDepartment(@Param("departmentIds") List<Long> departmentIds, Pageable pageable);

    /**
     * Выполняет поиск контактов по ФИО в указанных департаментах.
     * Комбинированный поиск с фильтрацией по любому полю ФИО и департаментам.
     * EntityGraph загружает все телефоны для избежания N+1.
     *
     * @param query поисковый запрос для поиска (без учета регистра)
     * @param departmentIds список идентификаторов департаментов для фильтрации
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных контактов с телефонами
     */
    @EntityGraph(attributePaths = {"workPhones", "workMobilePhones", "personalPhones"})
    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department d WHERE " +
           "(LOWER(c.lastName) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(c.firstName) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(c.middleName) LIKE LOWER(concat('%', :query, '%'))) " +
           "AND d.id IN :departmentIds",
        countQuery = "SELECT count(c) FROM Contact c WHERE " +
           "(LOWER(c.lastName) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(c.firstName) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(c.middleName) LIKE LOWER(concat('%', :query, '%'))) " +
           "AND c.department.id IN :departmentIds")
    Page<Contact> findByNameAndDepartmentIds(@Param("query") String query, @Param("departmentIds") List<Long> departmentIds, Pageable pageable);

}
