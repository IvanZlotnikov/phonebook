package com.ivanzlotnikov.phone_book.phonebook.exception;

/**
 * Исключение, выбрасываемое при попытке создать ресурс с уже существующими уникальными данными.
 * Например, пользователь с существующим username.
 */
public class DuplicateResourceException extends PhonebookException{

    /**
     * Создает исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Создает исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Фабричный метод для создания исключения о дублирующемся ресурсе.
     *
     * @param resourceName название типа ресурса
     * @param fieldName название поля с дубликатом
     * @param fieldValue значение поля
     * @return новый экземпляр исключения
     */
    public static DuplicateResourceException of(String resourceName, String fieldName, String fieldValue) {
        return new DuplicateResourceException(
            String.format("%s с %s '%s' уже существует", resourceName, fieldName, fieldValue)
        );
    }
}
