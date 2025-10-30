package com.ivanzlotnikov.phonebook.controller;

import com.ivanzlotnikov.phonebook.auth.service.UserService;
import com.ivanzlotnikov.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phonebook.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для главной страницы приложения.
 * Отображает адаптивную страницу с разным контентом для разных ролей.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ContactService contactService;
    private final DepartmentService departmentService;
    private final UserService userService;

    /**
     * Отображает главную страницу с общей информацией.
     * Для неавторизованных - приглашение войти
     * Для пользователей - статистика контактов и подразделений
     * Для администраторов - расширенная статистика и панель управления
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона главной страницы
     */
    @GetMapping({"/","/home"})
    public String home(Model model) {
            model.addAttribute("totalContacts", contactService.count());
            model.addAttribute("totalDepartments", departmentService.count());
            model.addAttribute("totalAdmins", userService.countAdmins());
            model.addAttribute("totalUsers", userService.countUsers());
            return "home/index";
    }

}
