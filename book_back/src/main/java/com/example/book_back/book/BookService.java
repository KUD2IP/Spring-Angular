package com.example.book_back.book;


import com.example.book_back.common.PageResponse;
import com.example.book_back.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.book_back.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    /**
     * Сохраняет новую книгу в базе данных с указанными данными.
     *
     * @param request объект с данными новой книги
     * @param connectedUser пользователь, который выполняет запрос
     * @return идентификатор сохраненной книги
     * @throws EntityNotFoundException если пользователь не найден
     */
    public Long save(BookRequest request, Authentication connectedUser) {
        // Получаем пользователя из аутентификационных данных
        User user = (User) connectedUser.getPrincipal();

        // Создаем новую книгу на основе данных запроса
        Book book = bookMapper.toBook(request);

        // Устанавливаем владельца книги
        book.setOwner(user);

        // Сохраняем книгу в базе данных и возвращаем ее идентификатор
        return bookRepository.save(book).getId();
    }

    /**
     * Поиск книги по идентификатору.
     *
     * @param bookId идентификатор книги
     * @return ответ с данными книги, если найдена, иначе бросает исключение EntityNotFoundException
     * @throws EntityNotFoundException если книга с указанным идентификатором не найдена
     */
    public BookResponse findById(Long bookId) {
        // Поиск книги в репозитории по идентификатору
        return bookRepository.findById(bookId)
                // Преобразование найденной книги в ответный объект
                .map(bookMapper::toBookResponse)
                // Если книга не найдена, бросаем исключение EntityNotFoundException
                .orElseThrow(() -> new EntityNotFoundException("Книга с идентификатором " + bookId + " не найдена"));
    }

    /**
     * Метод для получения всех книг, доступных для пользователя.
     * Возвращает страницованный ответ с информацией о книгах.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param connectedUser аутентифицированный пользователь
     * @return страницованный ответ с информацией о книгах
     */
    public PageResponse<BookResponse> findAllBooks(
            int page,
            int size,
            Authentication connectedUser
    ) {
        // Получаем аутентифицированного пользователя
        User user = (User) connectedUser.getPrincipal();

        // Создаем объект Pageable для разбиения книг на страницы
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        // Получаем все книги, доступные для пользователя
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());

        // Преобразуем книги в BookResponse и сохраняем в список
        List<BookResponse> bookResponses = books.getContent().stream()
                .map(bookMapper::toBookResponse)
                .toList();

        // Создаем и возвращаем страницованный ответ с информацией о книгах
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    /**
     * Возвращает список книг, принадлежащих указанному пользователю.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param connectedUser аутентифицированный пользователь
     * @return страница ответа с книгами
     */
    public PageResponse<BookResponse> findAllBooksByOwner(
            int page,
            int size,
            Authentication connectedUser
    ) {
        // Получаем аутентифицированного пользователя
        User user = (User) connectedUser.getPrincipal();

        // Создаем объект Pageable для управления пагинацией
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        // Получаем список книг, принадлежащих указанному пользователю
        Page<Book> books = bookRepository.findAll(withOwnerId(user.getId()), pageable);

        // Преобразуем список книг в список BookResponse
        List<BookResponse> bookResponses = books.getContent().stream()
                .map(bookMapper::toBookResponse)
                .toList();

        // Создаем объект PageResponse с данными о книгах
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }
}
