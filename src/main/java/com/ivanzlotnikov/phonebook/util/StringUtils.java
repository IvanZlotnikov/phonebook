package com.ivanzlotnikov.phonebook.util;

/**
 * Утилитный класс для работы со строками.
 * Устраняет дублирование операций trim() по всему проекту.
 */
public final class StringUtils {
    
    private StringUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Безопасно обрезает пробелы из строки
     * @param value строка для обработки
     * @return обрезанная строка или null, если входная строка null
     */
    public static String trimSafely(String value) {
        return value != null ? value.trim() : null;
    }
    
    /**
     * Проверяет, что строка не пустая после trim
     * @param value строка для проверки
     * @return true, если строка не null и не пустая после trim
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Проверяет, что строка пустая или null
     * @param value строка для проверки
     * @return true, если строка null или пустая после trim
     */
    public static boolean isBlank(String value) {
        return !isNotBlank(value);
    }
    
    /**
     * Форматирует ФИО в формат "Фамилия И.О."
     * @param lastName фамилия
     * @param firstName имя
     * @param middleName отчество (может быть null)
     * @return отформатированное ФИО
     */
    public static String formatFullName(String lastName, String firstName, String middleName) {
        StringBuilder result = new StringBuilder();
        
        if (isNotBlank(lastName)) {
            result.append(lastName.trim());
        }
        
        if (isNotBlank(firstName)) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(firstName.trim().charAt(0)).append(".");
        }
        
        if (isNotBlank(middleName)) {
            if (result.length() > 0 && isNotBlank(firstName)) {
                result.append(middleName.trim().charAt(0)).append(".");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Форматирует полное ФИО в формат "Фамилия Имя Отчество"
     * @param lastName фамилия
     * @param firstName имя
     * @param middleName отчество (может быть null)
     * @return полное ФИО
     */
    public static String formatFullNameComplete(String lastName, String firstName, String middleName) {
        StringBuilder result = new StringBuilder();
        
        if (isNotBlank(lastName)) {
            result.append(lastName.trim());
        }
        
        if (isNotBlank(firstName)) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(firstName.trim());
        }
        
        if (isNotBlank(middleName)) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(middleName.trim());
        }
        
        return result.toString();
    }
}
