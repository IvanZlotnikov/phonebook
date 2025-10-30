package com.ivanzlotnikov.phonebook.department.entity;

import com.ivanzlotnikov.phonebook.contact.entity.Contact;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность департамента (отдела) организации. Представляет иерархическую структуру подразделений с
 * поддержкой родительских и дочерних департаментов, а также связанных контактов.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@ToString(exclude = {"parentDepartment", "childrenDepartments", "contacts"})
@EqualsAndHashCode(exclude = {"parentDepartment", "childrenDepartments", "contacts"})
public class Department {

    private static final int NAME_FIELD_LENGTH = 100;
    private static final String DEPARTMENT_NAME_MANDATORY = "Department name is mandatory";

    /**
     * Уникальный идентификатор департамента.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название департамента. Обязательное уникальное поле, максимальная длина 100 символов.
     */
    @NotBlank(message = DEPARTMENT_NAME_MANDATORY)
    @Column(nullable = false, unique = true, length = NAME_FIELD_LENGTH)
    private String name;

    /**
     * Родительский департамент в иерархии. Связь Many-to-One с ленивой загрузкой. Null для
     * департаментов верхнего уровня.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    /**
     * Список дочерних департаментов. При удалении родительского департамента все дочерние также
     * удаляются (orphanRemoval). Каскадное удаление применяется ко всем операциям.
     */
    @OneToMany(mappedBy = "parentDepartment",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    List<Department> childrenDepartments = new ArrayList<>();

    /**
     * Список контактов (сотрудников), относящихся к данному департаменту. Связь One-to-Many с
     * ленивой загрузкой. Каскадные операции не применяются.
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY, cascade = {})
    private List<Contact> contacts;

}
