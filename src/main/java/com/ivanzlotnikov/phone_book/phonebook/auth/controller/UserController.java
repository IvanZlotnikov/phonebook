package com.ivanzlotnikov.phone_book.phonebook.auth.controller;

import com.ivanzlotnikov.phone_book.phonebook.auth.dto.UserDTO;
import com.ivanzlotnikov.phone_book.phonebook.auth.dto.UserFormDTO;
import com.ivanzlotnikov.phone_book.phonebook.auth.entity.User;
import com.ivanzlotnikov.phone_book.phonebook.auth.mapper.UserMapper;
import com.ivanzlotnikov.phone_book.phonebook.auth.service.UserService;
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
 * Контроллер для управления пользователями системы.
 * Предоставляет интерфейс для CRUD операций с пользователями.
 * Доступен только для администраторов.
 */
@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Отображает список всех пользователей системы.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона списка пользователей
     */
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOs = users.stream()
            .map(userMapper::toDto)
            .toList();
        model.addAttribute("users", userDTOs);
        return "users/list";
    }

    /**
     * Отображает форму создания нового пользователя.
     *
     * @param model модель для передачи данных
     * @return имя шаблона формы создания
     */
    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new UserFormDTO());
        model.addAttribute("isEdit", false);
        return "users/form";
    }

    /**
     * Отображает форму редактирования пользователя.
     *
     * @param id идентификатор пользователя
     * @param model модель для передачи данных
     * @param redirectAttributes атрибуты для сообщений
     * @return имя шаблона формы редактирования
     */
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.findById(id)
            .map(user -> {
                model.addAttribute("user", userMapper.userToFormDTO(user));
                model.addAttribute("isEdit", true);
                return "users/form";
            })
            .orElseGet(() -> {
                redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден");
                return "redirect:/users";
            });
    }

    /**
     * Обрабатывает сохранение пользователя (создание или обновление).
     *
     * @param userFormDTO данные пользователя из формы
     * @param bindingResult результаты валидации
     * @param model модель для передачи данных
     * @param redirectAttributes атрибуты для сообщений
     * @return редирект или возврат к форме при ошибках
     */
    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") UserFormDTO userFormDTO,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        // Проверка на существующего пользователя при создании
        if (userFormDTO.getId() == null && userService.usernameExists(userFormDTO.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", 
                "Пользователь с таким именем уже существует");
        }

        // Проверка пароля при создании нового пользователя
        if (userFormDTO.getId() == null && 
            (userFormDTO.getPassword() == null || userFormDTO.getPassword().trim().isEmpty())) {
            bindingResult.rejectValue("password", "required", "Пароль обязателен при создании пользователя");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", userFormDTO.getId() != null);
            return "users/form";
        }

        try {
            User user = userMapper.toEntity(userFormDTO);
            userService.save(user);
            
            String message = userFormDTO.getId() == null ? 
                "Пользователь успешно создан" : "Пользователь успешно обновлен";
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/users";
        } catch (Exception e) {
            log.error("Error saving user", e);
            model.addAttribute("errorMessage", "Ошибка при сохранении пользователя: " + e.getMessage());
            model.addAttribute("isEdit", userFormDTO.getId() != null);
            return "users/form";
        }
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя для удаления
     * @param redirectAttributes атрибуты для сообщений
     * @return редирект на список пользователей
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно удален");
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Ошибка при удалении пользователя: " + e.getMessage());
        }
        return "redirect:/users";
    }
}
