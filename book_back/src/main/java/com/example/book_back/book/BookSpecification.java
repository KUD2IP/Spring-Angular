package com.example.book_back.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    /**
     * Создает спецификацию для поиска книг, принадлежащих определенному владельцу.
     * @param ownerId идентификатор владельца
     * @return спецификация для поиска книг
     */
    public static Specification<Book> withOwnerId(Long ownerId) {
        // Создаем спецификацию, которая проверяет, что идентификатор владельца книги
        // совпадает с переданным идентификатором владельца.
        return (root, query, criteriaBuilder) ->
            // Получаем атрибут "owner" из корневого объекта запроса
            // и затем получаем атрибут "id" из атрибута "owner".
            // Затем мы сравниваем значение атрибута "id" с переданным идентификатором владельца.
           criteriaBuilder.equal(root.get("owner").get("id").as(Long.class), ownerId);
    }
}
