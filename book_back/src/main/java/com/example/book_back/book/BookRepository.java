package com.example.book_back.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Возвращает список всех книг, которые не архивированы, доступны для общего доступа и не принадлежат пользователю с указанным идентификатором.
     *
     * @param pageable объект Pageable для разбиения результата на страницы
     * @param userId идентификатор пользователя, книги которого не должны быть включены в результат
     * @return страница книг, отфильтрованных по указанным критериям
     */
    @Query("""
        SELECT book
        FROM Book book
        WHERE book.archived = false
        AND book.shareable = true
        AND book.owner.id != :userId
        """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, Long userId);

}
