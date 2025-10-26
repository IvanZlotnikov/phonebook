package com.ivanzlotnikov.phone_book.phonebook.config;

import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Перехватывает исключения и преобразует их в удобные для пользователя сообщения.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_ATTRIBUTE = "errorMessage";
    private static final String DEFAULT_REDIRECT = "redirect:/";

    /**
     * Обрабатывает исключения EntityNotFoundException.
     *
     * @param e исключение о ненайденной сущности
     * @param redirectAttributes атрибуты для передачи сообщений
     * @param request HTTP-запрос
     * @return URL для редиректа
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException e,
        RedirectAttributes redirectAttributes,
        HttpServletRequest request) {
        log.warn("Entity not found: {}", e.getMessage());
        return redirectWithError(redirectAttributes, "Запись не найдена", request);
    }

    /**
     * Обрабатывает исключения IllegalStateException.
     *
     * @param e исключение некорректного состояния
     * @param redirectAttributes атрибуты для передачи сообщений
     * @param request HTTP-запрос
     * @return URL для редиректа
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e,
        RedirectAttributes redirectAttributes,
        HttpServletRequest request) {
        log.warn("Illegal state: {}", e.getMessage());
        return redirectWithError(redirectAttributes, e.getMessage(), request);
    }

    /**
     * Обрабатывает все остальные необработанные исключения.
     *
     * @param e общее исключение
     * @param redirectAttributes атрибуты для передачи сообщений
     * @param request HTTP-запрос
     * @return URL для редиректа
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e,
        RedirectAttributes redirectAttributes,
        HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        return redirectWithError(redirectAttributes, "Произошла непредвиденная ошибка", request);
    }

    /**
     * Формирует редирект с сообщением об ошибке.
     *
     * @param redirectAttributes атрибуты для передачи сообщений
     * @param message текст сообщения об ошибке
     * @param request HTTP-запрос для получения referer URL
     * @return URL для редиректа
     */
    private String redirectWithError(RedirectAttributes redirectAttributes,
        String message,
        HttpServletRequest request) {
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, message);
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : DEFAULT_REDIRECT;
    }
}
