package com.example.book_back.book;

import org.springframework.stereotype.Service;

@Service
public class BookMapper {

    /**
     * Преобразует объект BookRequest в объект Book.
     *
     * @param request объект BookRequest для преобразования
     * @return объект Book, полученный из BookRequest
     */
    public Book toBook(BookRequest request) {

        // Используем билдер Book.builder() для создания нового объекта Book
        return Book.builder()
                // Устанавливаем идентификатор книги
                .id(request.id())
                // Устанавливаем заголовок книги
                .title(request.title())
                // Устанавливаем имя автора книги
                .authorName(request.authorName())
                // Устанавливаем ISBN книги
                .isbn(request.isbn())
                // Устанавливаем синопсис книги
                .synopsis(request.synopsis())
                // Устанавливаем флаг архивации книги в false
                .archived(false)
                // Устанавливаем флаг разрешения общего доступа к книге
                .shareable(request.shareable())
                // Создаем и возвращаем объект Book
                .build();
    }

    public BookResponse toBookResponse(Book book) {

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .archived(book.isArchived())
                .shareable(book.isShareable())
                .owner(book.getOwner().getFullName())
                //TODO: add cover
                .build();
    }
}
