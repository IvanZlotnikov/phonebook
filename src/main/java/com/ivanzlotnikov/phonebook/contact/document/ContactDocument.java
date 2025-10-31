//package com.ivanzlotnikov.phonebook.contact.document;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//
///**
// * Документ для индексации контактов в Elasticsearch.
// * Используется для full-text поиска по имени, позиции и телефонам.
// */
//@Document(indexName = "contacts")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class ContactDocument {
//
//    @Id
//    private String id;
//
//    @Field(type = FieldType.Text, analyzer = "russian")
//    private String fullName;
//
//    @Field(type = FieldType.Text, analyzer = "russian")
//    private String position;
//
//    @Field(type = FieldType.Keyword)
//    private Long departmentId;
//
//    @Field(type = FieldType.Text)
//    private String departmentName;
//
//    @Field(type = FieldType.Text)
//    private String workPhones;
//
//    @Field(type = FieldType.Text)
//    private String personalPhones;
//
//    @Field(type = FieldType.Text)
//    private String workMobilePhones;
//}
