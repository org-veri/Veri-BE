package org.veri.be.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.entity.Book;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookCommandService {

    private final BookRepository bookRepository;

    public Long addBook(String title, String image, String author, String publisher, String isbn) {

        Optional<Book> findBook = bookRepository.findBookByIsbn(isbn);
        if (findBook.isPresent()) {
            return findBook.get().getId();
        }

        Book book = Book.builder()
                .title(title)
                .image(image)
                .author(author)
                .publisher(publisher)
                .isbn(isbn)
                .build();

        bookRepository.save(book);
        return book.getId();
    }
}
