package com.ivanzlotnikov.phonebook.exception;

/**
 * Исключение, выбрасываемое при обнаружении невалидных бизнес-данных. Используется для проверок,
 * которые не покрываются стандартной валидацией.
 */
public class InvalidDataException extends PhonebookException {

    /**
     * Создает исключение с сообщением о невалидных данных.
     *
     * @param message сообщение об ошибке
     */
    public InvalidDataException(String message) {
        super(message);
    }

    /**
     * Создает исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Фабричный метод для создания исключения с указанием поля и причины.
     *
     * @param fieldName название поля
     * @param reason причина, по которой данные считаются невалидными
     * @return новый экземпляр исключения
     */
    public static InvalidDataException forField(String fieldName, String reason) {
        return new InvalidDataException(
            String.format("Поле '%s' содержит невалидные данные: %s", fieldName, reason)
        );
    }

    /**
     * Фабричный метод для создания исключения с указанием ресурса и описанием ошибки.
     *
     * @param resourceName название ресурса
     * @param description описание ошибки
     * @return новый экземпляр исключения
     */
    public static InvalidDataException forResource(String resourceName, String description) {
        return new InvalidDataException(
            String.format("Невалидные данные для ресурса '%s': %s", resourceName, description)
        );
    }
}
