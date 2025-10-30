package com.ivanzlotnikov.phonebook.department.controller;

import com.ivanzlotnikov.phonebook.department.dto.DepartmentDTO;
import com.ivanzlotnikov.phonebook.department.service.DepartmentService;
import com.ivanzlotnikov.phonebook.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для управления департаментами через веб-интерфейс. Обрабатывает HTTP-запросы для
 * операций CRUD департаментов. Использует Thymeleaf для рендеринга представлений.
 */
@Slf4j
@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentValidator departmentValidator;

    /**
     * Отображает список всех департаментов.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона для отображения списка департаментов
     */
    @GetMapping
    public String listDepartments(Model model) {
        log.info("Fetching all departments");
        List<DepartmentDTO> departments = departmentService.findAll();
        model.addAttribute("departments", departments);
        return "departments/list";
    }

    /**
     * Отображает форму для создания нового департамента.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы создания департамента
     */
    @GetMapping("/new")
    public String newDepartmentForm(Model model) {
        log.info("Showing form for new department");
        model.addAttribute("department", new DepartmentDTO());
        model.addAttribute("allDepartments", departmentService.findAll());
        return "departments/form";
    }

    /**
     * Сохраняет новый департамент.
     *
     * @param departmentDTO      данные департамента из формы
     * @param bindingResult      результат валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список департаментов или обратно на форму при ошибках
     */
    @PostMapping("/save")
    public String saveDepartment(@Valid @ModelAttribute("department") DepartmentDTO departmentDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model) {
        log.info("Saving department: {}", departmentDTO.getName());

        departmentValidator.checkForDuplicate(departmentDTO);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while saving department: {}", bindingResult.getAllErrors());
            model.addAttribute("allDepartments", departmentService.findAll());
            return "departments/form";
        }

        departmentService.save(departmentDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Департамент успешно сохранен");
        return "redirect:/departments";
    }

    /**
     * Отображает форму для редактирования департамента.
     *
     * @param id    идентификатор департамента
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы редактирования или редирект при ошибке
     */
    @GetMapping("/edit/{id}")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        log.info("Editing department with id: {}", id);

        DepartmentDTO department = departmentService.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.byId("Департамент", id));
        model.addAttribute("department", department);
        model.addAttribute("allDepartments", departmentService.findAll());
        return "departments/form";
    }

    /**
     * Удаляет департамент по идентификатору.
     *
     * @param id                 идентификатор департамента
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список департаментов
     */
    @PostMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id,
        RedirectAttributes redirectAttributes) {
        log.info("Deleting department with id: {}", id);

        departmentService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Департамент успешно удален");

        return "redirect:/departments";
    }
}
