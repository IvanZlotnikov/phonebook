package com.ivanzlotnikov.phone_book.phonebook.config;

import com.ivanzlotnikov.phone_book.phonebook.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException e,
                                               RedirectAttributes redirectAttributes) {
        log.warn("Entity not found: {}", e.getMessage());
        return redirectWithError(redirectAttributes, "Запись не найдена");
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e,
                                              RedirectAttributes redirectAttributes) {
        log.warn("Illegal state: {}", e.getMessage());
        return redirectWithError(redirectAttributes, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Unexpected error occurred", e);
        return redirectWithError(redirectAttributes, "Произошла непредвиденная ошибка");
    }

    private String redirectWithError(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/contacts";
    }
}
