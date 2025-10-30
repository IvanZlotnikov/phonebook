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
}
