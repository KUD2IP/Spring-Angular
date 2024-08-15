package com.example.book_back.book;

import com.example.book_back.common.BaseEntity;
import com.example.book_back.feedback.Feedback;
import com.example.book_back.history.BookTransactionHistory;
import com.example.book_back.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_t")
public class Book extends BaseEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "synopsis")
    private String synopsis;

    @Column(name = "book_cover")
    private String bookCover;

    @Column(name = "archived")
    private boolean archived;

    @Column(name = "shareable")
    private boolean shareable;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    /**
     * Возвращает средний рейтинг книги из всех оценок, полученных за нее.
     * Если нет оценок, возвращает 0.0.
     *
     * @return средний рейтинг книги или 0.0, если нет оценок
     */
    @Transient
    public double getRate() {
        // Проверяем, есть ли оценки
        if (feedbacks == null || feedbacks.isEmpty()) {
            // Если нет оценок, возвращаем 0.0
            return 0.0;
        }

        // Вычисляем средний рейтинг
        var rate = this.feedbacks
                .stream() // Преобразуем список оценок в поток
                .mapToDouble(Feedback::getNote) // Извлекаем оценку из каждой оценки
                .average() // Вычисляем среднее значение оценок
                .orElse(0.0); // Если нет оценок, возвращаем 0.0

        // Округляем средний рейтинг до одного знака после запятой
        double roundedRate = Math.round(rate * 10.0) / 10.0;

        return roundedRate;
    }

}
