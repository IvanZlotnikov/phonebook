package com.ivanzlotnikov.phone_book.phonebook.contact.entity;

import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

/**
 * Сущность контакта в телефонном справочнике. Представляет информацию о сотруднике организации,
 * включая ФИО, должность, департамент и различные типы телефонных номеров.
 */
@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
public class Contact {

    /**
     * Уникальный идентификатор контакта.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Полное имя сотрудника (ФИО). Обязательное поле, максимальная длина 100 символов.
     */
    @NotBlank(message = "Full name is mandatory")
    @Column(nullable = false, length = 100)
    private String fullName;

    /**
     * Должность сотрудника. Обязательное поле, максимальная длина 100 символов.
     */
    @NotBlank(message = "Position is mandatory")
    @Column(nullable = false, length = 100)
    private String position;

    /**
     * Департамент, к которому относится сотрудник. Связь Many-to-One с ленивой загрузкой.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * Список служебных стационарных телефонных номеров. Хранится в отдельной таблице
     * contact_work_phones. Использует батчинг для оптимизации загрузки (размер батча 50).
     */
    @ElementCollection
    @CollectionTable(name = "contact_work_phones",
        joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number", length = 20)
    @BatchSize(size = 50)
    private List<String> workPhones = new ArrayList<>();

    /**
     * Список личных телефонных номеров. Хранится в отдельной таблице contact_personal_phones.
     * Использует батчинг для оптимизации загрузки (размер батча 50).
     */
    @ElementCollection
    @CollectionTable(name = "contact_personal_phones",
        joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    @BatchSize(size = 50)
    private List<String> personalPhones = new ArrayList<>();

    /**
     * Список служебных мобильных телефонных номеров. Хранится в отдельной таблице
     * contact_work_mobile_phones. Использует батчинг для оптимизации загрузки (размер батча 50).
     */
    @ElementCollection
    @CollectionTable(name = "contact_work_mobile_phones", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    @BatchSize(size = 50)
    private List<String> workMobilePhones = new ArrayList<>();

    /**
     * Сравнивает текущий контакт с другим объектом на основе идентификатора.
     *
     * @param obj объект для сравнения
     * @return true, если объекты равны, иначе false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Contact)) {
            return false;
        }
        Contact contact = (Contact) obj;
        return Objects.equals(id, contact.id);
    }

    /**
     * Вычисляет хеш-код контакта на основе идентификатора.
     *
     * @return хеш-код контакта
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
