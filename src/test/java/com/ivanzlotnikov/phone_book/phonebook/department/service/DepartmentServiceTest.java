//package com.ivanzlotnikov.phone_book.phonebook.department.service;
//
//import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
//import com.ivanzlotnikov.phone_book.phonebook.department.repository.DepartmentRepository;
//import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class DepartmentServiceTest {
//
//    @Mock
//    private DepartmentRepository departmentRepository;
//
//    @InjectMocks
//    private DepartmentService departmentService;
//
//    @Test
//    void getDepartmentsHierarchy_ShouldReturnAllChildDepartments() {
//        // Given
//        Department parent = new Department();
//        parent.setId(1L);
//        parent.setName("IT Отдел");
//
//        Department child1 = new Department();
//        child1.setId(2L);
//        child1.setName("Разработка");
//
//        Department child2 = new Department();
//        child2.setId(3L);
//        child2.setName("Тестирование");
//
//        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
//        when(departmentRepository.findByParentDepartment(parent)).thenReturn(List.of(child1, child2));
//        when(departmentRepository.findByParentDepartment(child1)).thenReturn(List.of());
//        when(departmentRepository.findByParentDepartment(child2)).thenReturn(List.of());
//
//        // When
//        List<Department> result = departmentService.getDepartmentsHierarchy(1L);
//
//        // Then
//        assertEquals(2, result.size());
//        assertTrue(result.stream().anyMatch(d -> d.getName().equals("Разработка")));
//        assertTrue(result.stream().anyMatch(d -> d.getName().equals("Тестирование")));
//    }
//
//    @Test
//    void getDepartmentsHierarchy_ShouldThrowException_WhenDepartmentNotFound() {
//        // Given
//        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        assertThrows(EntityNotFoundException.class, () -> {
//            departmentService.getDepartmentsHierarchy(999L);
//        });
//    }
//}