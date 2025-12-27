package org.veri.be.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.book.client.BookSearchClient;
import org.veri.be.domain.book.client.NaverClientException;
import org.veri.be.domain.book.dto.book.BookConverter;
import org.veri.be.domain.book.dto.book.BookSearchResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.exception.BookErrorCode;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookSearchClient bookSearchClient;

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

    /**
     * Naver OpenAPI 활용해 책의 정보를 보여주는 메서드
     */
    public BookSearchResponse searchBook(String query, int page, int size) {
        try {
            return BookConverter.toBookSearchResponse(bookSearchClient.search(query, page, size));
        } catch (NaverClientException _) {
            throw ApplicationException.of(BookErrorCode.BAD_REQUEST);
        }
    }

    public Book getBookById(Long bookId) {
        if (bookId == null) return null;
        return bookRepository.findById(bookId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND));
    }
}
