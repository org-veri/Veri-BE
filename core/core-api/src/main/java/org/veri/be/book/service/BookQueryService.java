package org.veri.be.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.client.BookSearchClient;
import org.veri.be.book.client.NaverClientException;
import org.veri.be.book.dto.book.BookConverter;
import org.veri.be.book.dto.book.BookSearchResponse;
import org.veri.be.book.entity.Book;
import org.veri.be.book.exception.BookErrorCode;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookQueryService {

    private final BookRepository bookRepository;
    private final BookSearchClient bookSearchClient;

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
