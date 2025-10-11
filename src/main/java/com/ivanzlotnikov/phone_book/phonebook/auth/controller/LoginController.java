package com.ivanzlotnikov.phone_book.phonebook.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password) {
        // Spring Security автоматически обработает аутентификацию
        // Мы просто перенаправляем на главную страницу
        return "redirect:/contacts";
    }

}
