package com.ivanzlotnikov.phone_book.phonebook.controller;

import com.ivanzlotnikov.phone_book.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для главной страницы приложения.
 * Отображает статистику по контактам и департаментам.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ContactService contactService;
    private final DepartmentService departmentService;

    /**
     * Отображает главную страницу с общей информацией.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона главной страницы
     */
    @GetMapping({"/","/home"})
    public String home(Model model) {
            model.addAttribute("totalContacts", contactService.count());
            model.addAttribute("totalDepartments", departmentService.count());
            return "home/index";
    }

}
