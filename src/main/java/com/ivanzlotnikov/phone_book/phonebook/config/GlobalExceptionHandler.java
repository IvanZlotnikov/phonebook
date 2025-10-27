package com.ivanzlotnikov.phone_book.phonebook.config;

import com.ivanzlotnikov.phone_book.phonebook.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_ATTRIBUTE = "errorMessage";
    private static final String DEFAULT_REDIRECT = "redirect:/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException e,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        log.warn("Resource not found: {}", e.getMessage());
        return redirectWithError(redirectAttributes, e.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicateResource(DuplicateResourceException e,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        log.warn("Duplicate resource: {}", e.getMessage());
        return redirectWithError(redirectAttributes, e.getMessage(), request);
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    public String handleInsufficientPermissions(InsufficientPermissionsException e,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        log.warn("Insufficient permissions: {}", e.getMessage());
        return redirectWithError(redirectAttributes, e.getMessage(), request);
    }

    @ExceptionHandler(InvalidDataException.class)
    public String handleInvalidData(InvalidDataException e,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        log.warn("Invalid data: {}", e.getMessage());
        return redirectWithError(redirectAttributes, e.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        log.warn("Illegal state: {}", e.getMessage());
        return redirectWithError(redirectAttributes, e.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        return redirectWithError(redirectAttributes, "Произошла непредвиденная ошибка", request);
    }

    private String redirectWithError(RedirectAttributes redirectAttributes,
                                     String message,
                                     HttpServletRequest request) {
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, message);
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : DEFAULT_REDIRECT;
    }
}
