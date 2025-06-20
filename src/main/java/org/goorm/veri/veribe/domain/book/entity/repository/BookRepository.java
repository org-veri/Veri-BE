package org.goorm.veri.veribe.domain.book.entity.repository;

import org.goorm.veri.veribe.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findBookByTitle(String title);
}
