package com.ivanzlotnikov.phone_book.phonebook.contact.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Вспомогательный класс для построения URL редиректов с сохранением контекста поиска.
 * Используется для возврата пользователя на предыдущую страницу с фильтрами и пагинацией.
 */
@Component
public class ContactRedirectBuilder {

    private static final String CONTACTS_PATH = "/contacts";

    /**
     * Строит URL для редиректа с параметрами поиска и пагинации.
     *
     * @param searchQuery поисковый запрос
     * @param departmentId идентификатор департамента для фильтрации
     * @param page номер страницы
     * @return сформированный URL для редиректа
     */
    public String buildRedirectUrl(String searchQuery, Long departmentId, Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("redirect:" + CONTACTS_PATH);

        addSearchParam(builder, searchQuery);
        addDepartmentParam(builder, departmentId);
        addPageParam(builder, page);

        return builder.toUriString();
    }

    private void addSearchParam(UriComponentsBuilder builder, String searchQuery) {
        if(searchQuery != null && !searchQuery.trim().isEmpty()){
            builder.queryParam("search", searchQuery);
        }
    }

    private void addDepartmentParam(UriComponentsBuilder builder, Long departmentId) {
        if(departmentId != null){
            builder.queryParam("dept", departmentId);
        }
    }

    private void addPageParam(UriComponentsBuilder builder, Integer page) {
        if(page != null && page > 0){
            builder.queryParam("page", page);
        }
    }

}
