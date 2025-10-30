package com.ivanzlotnikov.phonebook.department.repository;

import com.ivanzlotnikov.phonebook.department.dto.DepartmentWithContactCountDTO;
import com.ivanzlotnikov.phonebook.department.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью {@link Department}.
 * Предоставляет методы для работы с иерархической структурой департаментов,
 * включая рекурсивные запросы и агрегацию данных.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Находит все корневые департаменты (без родительского департамента).
     *
     * @return список корневых департаментов
     */
    List<Department> findByParentDepartmentIsNull();

    /**
     * Находит прямых потомков указанного департамента.
     *
     * @param parentId идентификатор родительского департамента
     * @return список дочерних департаментов
     */
    List<Department> findByParentDepartmentId(Long parentId);

    /**
     * Находит департамент по точному названию.
     *
     * @param name название департамента
     * @return Optional с департаментом или пустой Optional
     */
    Optional<Department> findByName(String name);

    /**
     * Проверяет существование департамента с указанным названием.
     *
     * @param name название департамента
     * @return true, если департамент существует, иначе false
     */
    boolean existsByName(String name);

    /**
     * Находит все департаменты с указанным родительским департаментом.
     *
     * @param parent родительский департамент
     * @return список дочерних департаментов
     */
    List<Department> findByParentDepartment(Department parent);

    /**
     * Рекурсивно находит все поддепартаменты указанного департамента.
     * Использует Common Table Expression (CTE) для PostgreSQL.
     * Не включает сам департамент в результат.
     *
     * @param departmentId идентификатор департамента
     * @return список всех поддепартаментов на всех уровнях вложенности
     */
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

    /**
     * Выполняет поиск департаментов по части названия без учета регистра.
     *
     * @param name часть названия для поиска
     * @return список найденных департаментов
     */
    List<Department> findByNameContainingIgnoreCase(String name);

    /**
     * Получает все департаменты с количеством контактов в каждом.
     * Использует агрегацию для подсчета контактов и предотвращения N+1 проблемы.
     *
     * @return список DTO с департаментами и количеством контактов
     */
    @Query("SELECT new com.ivanzlotnikov.phonebook.department.dto.DepartmentWithContactCountDTO(d,COUNT(c.id)) " +
           "FROM Department d LEFT JOIN d.contacts c GROUP BY d.id")
    List<DepartmentWithContactCountDTO> findAllWithContactCount();

    /**
     * Получает корневые департаменты с количеством контактов.
     * Оптимизированный запрос для предотвращения N+1 проблемы.
     *
     * @return список DTO с корневыми департаментами и количеством контактов
     */
    @Query("""
        SELECT new com.ivanzlotnikov.phonebook.department.dto.DepartmentWithContactCountDTO(d, COUNT(c.id))
        FROM Department d
        LEFT JOIN d.contacts c
        WHERE d.parentDepartment IS NULL
        GROUP BY d.id, d.name, d.parentDepartment
        """)
    List<DepartmentWithContactCountDTO> findRootDepartmentsWithContactCount();

    /**
     * Выполняет поиск департаментов по названию с подсчетом контактов.
     * Оптимизированный запрос для предотвращения N+1 проблемы.
     *
     * @param name часть названия для поиска (без учета регистра)
     * @return список DTO с найденными департаментами и количеством контактов
     */
    @Query("""
        SELECT new com.ivanzlotnikov.phonebook.department.dto.DepartmentWithContactCountDTO(d, COUNT(c.id))
        FROM Department d
        LEFT JOIN d.contacts c
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))
        GROUP BY d.id, d.name, d.parentDepartment
        """)
    List<DepartmentWithContactCountDTO> findByNameWithContactCount(@Param("name") String name);

    /**
     * Загружает все департаменты с родительскими департаментами одним запросом.
     * Используется для построения иерархии департаментов в памяти.
     * Предотвращает N+1 проблему при работе с иерархией.
     *
     * @return список всех департаментов с загруженными родителями
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.parentDepartment")
    List<Department> findAllWithParent();
}
