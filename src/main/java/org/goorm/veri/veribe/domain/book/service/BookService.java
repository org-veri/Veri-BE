package org.goorm.veri.veribe.domain.book.service;

import org.goorm.veri.veribe.domain.book.dto.book.BookSearchResponse;

public interface BookService {

    Long addBook(String title, String image, String author, String publisher, String isbn);

    BookSearchResponse searchBook(String query, int page, int size);
}
