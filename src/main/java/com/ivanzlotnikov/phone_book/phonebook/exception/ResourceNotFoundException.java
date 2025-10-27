package com.ivanzlotnikov.phone_book.phonebook.exception;

/**
 * Исключение, выбрасываемое когда запрашиваемый ресурс не найден в системе. Используется для
 * пользователей, контактов, подразделений и т.д.
 */

public class ResourceNotFoundException extends PhonebookException {

    /**
     * Создает исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Создает исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина ошибки
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Фабричный метод для создания исключения с указанием типа ресурса и ID.
     *
     * @param resourceName название типа ресурса
     * @param id           идентификатор ресурса
     * @return новый экземпляр исключения
     */
    public static ResourceNotFoundException byId(String resourceName, Long id) {
        return new ResourceNotFoundException(
            String.format("%s с ID %d не найден", resourceName, id)
        );
    }

    /**
     * Фабричный метод для создания исключения с указанием типа ресурса и строкового
     * идентификатора.
     *
     * @param resourceName название типа ресурса
     * @param identifier   строковый идентификатор
     * @return новый экземпляр исключения
     */
    public static ResourceNotFoundException byIdentifier(String resourceName, String identifier) {
        return new ResourceNotFoundException(
            String.format("%s '%s' не найден", resourceName, identifier)
        );
    }
}
