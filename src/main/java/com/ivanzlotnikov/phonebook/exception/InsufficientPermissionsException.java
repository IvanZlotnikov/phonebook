package com.ivanzlotnikov.phonebook.exception;

/**
 * Исключение, выбрасываемое при попытке выполнить операцию без необходимых прав.
 */
public class InsufficientPermissionsException extends PhonebookException {

    /**
     * Создает исключение с сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public InsufficientPermissionsException(String message) {
        super(message);
    }

    /**
     * Создает исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина ошибки
     */
    public InsufficientPermissionsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Фабричный метод для создания исключения с форматированным сообщением об операции.
     *
     * @param operation название операции
     * @return новый экземпляр исключения
     */
    public static InsufficientPermissionsException forOperation(String operation) {
        return new InsufficientPermissionsException(
            String.format("Недостаточно прав для выполнения операции: %s", operation)
        );
    }

    /**
     * Фабричный метод для создания исключения с указанием пользователя и операции.
     *
     * @param username  имя пользователя
     * @param operation название операции
     * @return новый экземпляр исключения
     */
    public static InsufficientPermissionsException forUserAndOperation(String username,
        String operation) {
        return new InsufficientPermissionsException(
            String.format("Пользователь '%s' не имеет прав для выполнения операции: %s", username,
                operation)
        );
    }
}
