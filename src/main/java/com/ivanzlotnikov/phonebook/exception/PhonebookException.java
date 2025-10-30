package com.ivanzlotnikov.phonebook.exception;

/**
 * Базовое исключение для всех бизнес-исключений в приложении
 */
public abstract class PhonebookException extends RuntimeException {

    /**
     * Конструктор для создания исключения с сообщением
     *
     * @param message сообщение об ошибке
     */
    protected PhonebookException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением и причиной ошибки
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    protected PhonebookException(String message, Throwable cause) {
        super(message, cause);
    }
}
