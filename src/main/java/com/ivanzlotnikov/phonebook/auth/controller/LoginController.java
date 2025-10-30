package com.ivanzlotnikov.phonebook.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для обработки запросов аутентификации.
 * Отображает страницу входа в систему.
 */
@Controller
public class LoginController {

    /**
     * Отображает страницу входа в систему.
     *
     * @return имя шаблона страницы входа
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
