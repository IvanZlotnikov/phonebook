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

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is mandatory")
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Position is mandatory")
    @Column(nullable = false, length = 100)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    //служебные номера
    @ElementCollection
    @CollectionTable(name = "contact_work_phones",
        joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number", length = 20)
    @BatchSize(size = 50)
    private List<String> workPhones = new ArrayList<>();

    // личные номера
    @ElementCollection
    @CollectionTable(name = "contact_personal_phones",
        joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    @BatchSize(size = 50)
    private List<String> personalPhones = new ArrayList<>();

    //Служебные мобильные номера
    @ElementCollection
    @CollectionTable(name = "contact_work_mobile_phones", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    @BatchSize(size = 50)
    private List<String> workMobilePhones = new ArrayList<>();

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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
