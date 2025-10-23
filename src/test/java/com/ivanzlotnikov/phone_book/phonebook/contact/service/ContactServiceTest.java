//package com.ivanzlotnikov.phone_book.phonebook.contact.service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.ivanzlotnikov.phone_book.phonebook.contact.dto.ContactDTO;
//import com.ivanzlotnikov.phone_book.phonebook.contact.entity.Contact;
//import com.ivanzlotnikov.phone_book.phonebook.contact.repository.ContactRepository;
//import com.ivanzlotnikov.phone_book.phonebook.department.entity.Department;
//import com.ivanzlotnikov.phone_book.phonebook.department.repository.DepartmentRepository;
//import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class ContactServiceTest {
//
//    @Mock
//    private ContactRepository contactRepository;
//
//    @Mock
//    private DepartmentRepository departmentRepository;
//
//    @Mock
//    private DepartmentService departmentService;
//
//    @InjectMocks
//    private ContactService contactService;
//
//    private Contact testContact;
//    private Department testDepartment;
//
//    @BeforeEach
//    void setUp() {
//        testDepartment = new Department();
//        testDepartment.setId(1L);
//        testDepartment.setName("IT Отдел");
//
//        testContact = new Contact();
//        testContact.setId(1L);
//        testContact.setFullName("Иванов Иван Иванович");
//        testContact.setPosition("Разработчик");
//        testContact.setDepartment(testDepartment);
//        testContact.setWorkPhones(new ArrayList<>(List.of("+7 (495) 111-11-11")));
//        testContact.setWorkMobilePhones(new ArrayList<>(List.of("+7 (916) 111-11-11")));
//        testContact.setPersonalPhones(new ArrayList<>());
//    }
//
//    @Test
//    void deleteAllById_ShouldDeleteExistingContacts() {
//        // Given
//        List<Long> contactIds = List.of(1L, 2L, 3L);
//
//        when(contactRepository.existsById(1L)).thenReturn(true);
//        when(contactRepository.existsById(2L)).thenReturn(true);
//        when(contactRepository.existsById(3L)).thenReturn(false);
//
//        // When
//        contactService.deleteAllById(contactIds);
//
//        // Then
//        verify(contactRepository).deleteAllByIdIn(List.of(1L, 2L));
//    }
//
//    @Test
//    void deleteAllById_ShouldDoNothing_WhenEmptyList() {
//        // Given
//        List<Long> emptyList = new ArrayList<>();
//
//        // When
//        contactService.deleteAllById(emptyList);
//
//        // Then
//        verify(contactRepository, never()).deleteAllByIdIn(anyList());
//    }
//
//    @Test
//    void findByDepartmentHierarchy_ShouldReturnContactsFromHierarchy() {
//        // Given
//        Long departmentId = 1L;
//
//        Department childDepartment = new Department();
//        childDepartment.setId(2L);
//        childDepartment.setName("Разработка");
//
//        Contact childContact = new Contact();
//        childContact.setId(2L);
//        childContact.setFullName("Петров Петр");
//        childContact.setDepartment(childDepartment);
//
//        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
//        when(departmentService.getDepartmentsHierarchy(departmentId)).thenReturn(
//            new ArrayList<>(List.of(childDepartment)));
//        when(contactRepository.findByDepartmentIn(any())).thenReturn(
//            List.of(testContact, childContact));
//
//        // When
//        List<ContactDTO> result = contactService.findByDepartmentHierarchy(departmentId);
//
//        // Then
//        assertEquals(2, result.size());
//        assertTrue(result.stream().anyMatch(c -> c.getFullName().equals("Иванов Иван Иванович")));
//        assertTrue(result.stream().anyMatch(c -> c.getFullName().equals("Петров Петр")));
//    }
//
//    @Test
//    void findById_ShouldReturnContact_WhenExists() {
//        // Given
//        when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
//
//        // When
//        Optional<ContactDTO> result = contactService.findById(1L);
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals("Иванов Иван Иванович", result.get().getFullName());
//    }
//
//    @Test
//    void findById_ShouldReturnEmpty_WhenNotExists() {
//        // Given
//        when(contactRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When
//        Optional<ContactDTO> result = contactService.findById(999L);
//
//        // Then
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void searchByName_ShouldReturnMatchingContacts() {
//        // Given
//        String searchQuery = "Иванов";
//        when(contactRepository.findByFullNameContainingIgnoreCase(searchQuery))
//            .thenReturn(List.of(testContact));
//
//        // When
//        List<ContactDTO> result = contactService.searchByName(searchQuery);
//
//        // Then
//        assertEquals(1, result.size());
//        assertEquals("Иванов Иван Иванович", result.get(0).getFullName());
//    }
//
//    @Test
//    void existsByFullNameAndPosition_ShouldReturnTrue_WhenDuplicateExists() {
//        // Given
//        when(contactRepository.findByFullNameAndPosition("Иванов Иван Иванович", "Разработчик"))
//            .thenReturn(Optional.of(testContact));
//
//        // When
//        boolean result = contactService.existsByFullNameAndPosition("Иванов Иван Иванович",
//            "Разработчик");
//
//        // Then
//        assertTrue(result);
//    }
//}