package com.ivanzlotnikov.phone_book.phonebook.contact.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ContactRedirectBuilder {

    private static final String CONTACTS_PATH = "/contacts";

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
