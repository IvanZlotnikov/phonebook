//package com.ivanzlotnikov.phonebook.contact.document;
//
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
///**
// * Репозиторий для работы с Elasticsearch индексом контактов.
// */
//@Repository
//public interface ContactDocumentRepository extends ElasticsearchRepository<ContactDocument, String> {
//
//    /**
//     * Поиск контактов по запросу в fullName, position, departmentName.
//     *
//     * @param query поисковый запрос
//     * @return список найденных документов
//     */
//    List<ContactDocument> findByFullNameContainingOrPositionContainingOrDepartmentNameContaining(String query1, String query2, String query3);
//}
