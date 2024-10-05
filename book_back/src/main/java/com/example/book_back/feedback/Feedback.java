package com.example.book_back.feedback;

import com.example.book_back.book.Book;
import com.example.book_back.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "feedback_t")
public class Feedback extends BaseEntity {

    @Column(name = "note")
    private Double note; // 1 - 5 stars
    @Column(name = "comment")
    private String comment;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

}
