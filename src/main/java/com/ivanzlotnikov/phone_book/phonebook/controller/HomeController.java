package com.ivanzlotnikov.phone_book.phonebook.controller;

import com.ivanzlotnikov.phone_book.phonebook.contact.service.ContactService;
import com.ivanzlotnikov.phone_book.phonebook.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ContactService contactService;
    private final DepartmentService departmentService;

    @GetMapping({"/","/home"})
    public String home(Model model) {
            model.addAttribute("totalContacts", contactService.count());
            model.addAttribute("totalDepartments", departmentService.count());
            return "home/index";
    }

}
