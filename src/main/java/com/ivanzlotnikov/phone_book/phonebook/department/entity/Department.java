package com.ivanzlotnikov.phone_book.phonebook.department.entity;

import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "departments")
@Getter
@Setter
@ToString(exclude = {"parentDepartment", "childrenDepartments", "contacts"})
@EqualsAndHashCode(exclude = {"parentDepartment", "childrenDepartments", "contacts"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Department name is mandatory")
    @Column(nullable = false, unique = true,length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @OneToMany(mappedBy = "parentDepartment",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    List<Department> childrenDepartments = new ArrayList<>();

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY,cascade = {})
    private List<Contact> contacts = new ArrayList<>();

    //вспомогательные методы
    public void addChildDepartment(Department child) {
        childrenDepartments.add(child);
        child.setParentDepartment(this);
    }

    public void removeChildDepartment(Department child) {
        childrenDepartments.remove(child);
        child.setParentDepartment(null);
    }


}
