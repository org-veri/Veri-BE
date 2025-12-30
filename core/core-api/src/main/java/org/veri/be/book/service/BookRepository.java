package org.veri.be.book.service;

import org.veri.be.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findBookByIsbn(String isbn);
}
