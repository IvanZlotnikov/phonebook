package com.ivanzlotnikov.phonebook.contact.entity;

import com.ivanzlotnikov.phonebook.department.entity.Department;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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

    private static final int NAME_FIELD_LENGTH = 100;
    private static final int PHONE_NUMBER_FIELD_LENGTH = 20;
    private static final int BATCH_SIZE = 50;

    private static final String LAST_NAME_MANDATORY = "Фамилия обязательна для заполнения";
    private static final String FIRST_NAME_MANDATORY = "Имя обязательно для заполнения";
    private static final String POSITION_MANDATORY = "Position is mandatory";

    /**
     * Уникальный идентификатор контакта.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Фамилия сотрудника. Обязательное поле, максимальная длина 100 символов.
     */
    @NotBlank(message = LAST_NAME_MANDATORY)
    @Column(nullable = false, length = NAME_FIELD_LENGTH)
    private String lastName;

    /**
     * Имя сотрудника. Обязательное поле, максимальная длина 100 символов.
     */
    @NotBlank(message = FIRST_NAME_MANDATORY)
    @Column(nullable = false, length = NAME_FIELD_LENGTH)
    private String firstName;

    /**
     * Отчество сотрудника. Необязательное поле, максимальная длина 100 символов.
     */
    @Column(length = NAME_FIELD_LENGTH)
    private String middleName;

    /**
     * Должность сотрудника. Обязательное поле, максимальная длина 100 символов.
     */
    @NotBlank(message = POSITION_MANDATORY)
    @Column(nullable = false, length = NAME_FIELD_LENGTH)
    private String position;

    /**
     * Департамент, к которому относится сотрудник. Связь Many-to-One с ленивой загрузкой.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * Набор служебных стационарных телефонных номеров. Хранится в отдельной таблице
     * contact_work_phones. Set используется для избежания MultipleBagFetchException.
     */
    @ElementCollection
    @CollectionTable(name = "contact_work_phones",
        joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number", length = PHONE_NUMBER_FIELD_LENGTH)
    private Set<String> workPhones = new HashSet<>();

    /**
     * Набор личных телефонных номеров. Хранится в отдельной таблице contact_personal_phones.
     * Set используется для избежания MultipleBagFetchException.
     */
    @ElementCollection
    @CollectionTable(name = "contact_personal_phones",
        joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    private Set<String> personalPhones = new HashSet<>();

    /**
     * Набор служебных мобильных телефонных номеров. Хранится в отдельной таблице
     * contact_work_mobile_phones. Set используется для избежания MultipleBagFetchException.
     */
    @ElementCollection
    @CollectionTable(name = "contact_work_mobile_phones", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    private Set<String> workMobilePhones = new HashSet<>();

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
