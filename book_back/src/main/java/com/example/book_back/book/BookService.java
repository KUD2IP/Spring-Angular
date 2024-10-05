package com.example.book_back.book;


import com.example.book_back.common.PageResponse;
import com.example.book_back.exception.OperationNotPermittedException;
import com.example.book_back.file.FileStorageService;
import com.example.book_back.history.BookTransactionHistory;
import com.example.book_back.history.BookTransactionHistoryRepository;
import com.example.book_back.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.example.book_back.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private FileStorageService fileStorageService;

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

    /**
     * Метод для получения всех забронированных книг пользователя.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param connectedUser аутентифицированный пользователь
     * @return ответ с забронированными книгами и информацией о странице
     */
    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        // Получаем аутентифицированного пользователя
        User user = (User) connectedUser.getPrincipal();

        // Создаем объект Pageable с настройками страницы и сортировки
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        // Получаем все забронированные книги пользователя
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());

        // Преобразуем забронированные книги в ответную модель
        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        // Создаем объект PageResponse с данными о забронированных книгах
        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());

        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );

    }

    public Long updateShareableStatus(Long bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Long updateArchivedStatus(Long bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books archived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Long borrowBook(Long bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Book is not available for borrowing");
        }

        User user = (User) connectedUser.getPrincipal();

        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow own book");
        }

        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());

        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("You have already borrowed this book");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Long returnBorrowedBook(Long bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();

        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow own book");
        }

        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You have not borrowed this book yet"));

        bookTransactionHistory.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Long approveReturnBorrowedBook(Long bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();

        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow own book");
        }

        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));

        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();

    }

    public void uploadBookCoverPicture(Long bookId, MultipartFile file, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = (User) connectedUser.getPrincipal();
        var bookCover = fileStorageService.saveFile(file, user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
