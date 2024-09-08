package com.example.book_back.book;

import com.example.book_back.file.FileUtils;
import com.example.book_back.history.BookTransactionHistory;
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
                .rate(book.getRate())
                .archived(book.isArchived())
                .shareable(book.isShareable())
                .owner(book.getOwner().getFullName())
                .cover(FileUtils.readFileFromLocation(book.getBookCover()))
                .build();
    }

    /**
     * Преобразует запись истории транзакции книги в ответ о borrowed book.
     *
     * @param bookTransactionHistory Запись истории транзакции книги
     * @return BorrowedBookResponse - ответ о borrowed book
     */
    public BorrowedBookResponse toBorrowedBookResponse(BookTransactionHistory bookTransactionHistory) {
        // Создаем билдер BorrowedBookResponse
        return BorrowedBookResponse.builder()
                // Устанавливаем идентификатор книги
                .id(bookTransactionHistory.getBook().getId())
                // Устанавливаем название книги
                .title(bookTransactionHistory.getBook().getTitle())
                // Устанавливаем имя автора книги
                .authorName(bookTransactionHistory.getBook().getAuthorName())
                // Устанавливаем ISBN книги
                .isbn(bookTransactionHistory.getBook().getIsbn())
                // Устанавливаем рейтинг книги
                .rate(bookTransactionHistory.getBook().getRate())
                // Устанавливаем флаг возврата книги
                .returned(bookTransactionHistory.isReturned())
                // Устанавливаем флаг одобрения возврата книги
                .returnApproved(bookTransactionHistory.isReturnApproved())
                // Создаем и возвращаем объект BorrowedBookResponse
                .build();
    }
}
