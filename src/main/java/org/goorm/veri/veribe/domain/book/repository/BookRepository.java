package org.goorm.veri.veribe.domain.book.repository;

import org.goorm.veri.veribe.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findBookByIsbn(String isbn);
}
