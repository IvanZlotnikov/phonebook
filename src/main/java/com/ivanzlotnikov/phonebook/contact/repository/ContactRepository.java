package com.ivanzlotnikov.phonebook.contact.repository;

import com.ivanzlotnikov.phonebook.contact.entity.Contact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Выполняет поиск контактов по части имени (без учета регистра).
     * Загружает департамент вместе с контактом для избежания N+1 проблемы.
     *
     * @param name часть имени для поиска
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных контактов с департаментами
     */
    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE LOWER(c.fullName) LIKE LOWER(concat('%', :name, '%'))")
    Page<Contact> findByFullNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Проверяет существование контакта с указанными ФИО и должностью.
     *
     * @param fullName полное имя сотрудника
     * @param position должность сотрудника
     * @return true, если контакт существует, иначе false
     */
    boolean existsByFullNameAndPosition(String fullName, String position);

    /**
     * Находит контакт по идентификатору с загрузкой департамента.
     *
     * @param id идентификатор контакта
     * @return Optional с контактом и департаментом, или пустой Optional
     */
    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.department WHERE c.id = :id")
    Optional<Contact> findByIdWithDepartment(@Param("id") Long id);

    /**
     * Получает все контакты с загрузкой департаментов.
     * Использует отдельный запрос для подсчета общего количества записей.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница контактов с департаментами
     */
    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department",
        countQuery = "SELECT count(c) FROM Contact c")
    Page<Contact> findAllWithDepartment(Pageable pageable);

    /**
     * Находит контакты по списку идентификаторов департаментов.
     * Загружает департаменты вместе с контактами.
     *
     * @param departmentIds список идентификаторов департаментов
     * @param pageable параметры пагинации и сортировки
     * @return страница контактов из указанных департаментов
     */
    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department d WHERE d.id IN :departmentIds",
        countQuery = "SELECT count(c) FROM Contact c WHERE c.department.id IN :departmentIds")
    Page<Contact> findByDepartmentIdInWithDepartment(@Param("departmentIds") List<Long> departmentIds, Pageable pageable);

    /**
     * Выполняет поиск контактов по имени в указанных департаментах.
     * Комбинированный поиск с фильтрацией по имени и департаментам.
     *
     * @param name часть имени для поиска (без учета регистра)
     * @param departmentIds список идентификаторов департаментов для фильтрации
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных контактов
     */
    @Query(value = "SELECT c FROM Contact c LEFT JOIN FETCH c.department d WHERE LOWER(c.fullName) LIKE LOWER(concat('%', :name, '%')) AND d.id IN :departmentIds",
        countQuery = "SELECT count(c) FROM Contact c WHERE LOWER(c.fullName) LIKE LOWER(concat('%', :name, '%')) AND c.department.id IN :departmentIds")
    Page<Contact> findByNameAndDepartmentIds(@Param("name") String name, @Param("departmentIds") List<Long> departmentIds, Pageable pageable);

}
