package com.ivanzlotnikov.phonebook.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для хранения контекста поиска и пагинации.
 * Устраняет дублирование параметров поиска в контроллере.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchContext {
    
    private String searchQuery;
    private Long departmentId;
    private Integer page;
    
    /**
     * Проверяет, есть ли активный поисковый запрос
     */
    public boolean hasSearchQuery() {
        return searchQuery != null && !searchQuery.trim().isEmpty();
    }
    
    /**
     * Проверяет, выбран ли департамент для фильтрации
     */
    public boolean hasDepartment() {
        return departmentId != null;
    }
    
    /**
     * Возвращает нормализованный поисковый запрос
     */
    public String getNormalizedSearchQuery() {
        return hasSearchQuery() ? searchQuery.trim() : null;
    }
}
