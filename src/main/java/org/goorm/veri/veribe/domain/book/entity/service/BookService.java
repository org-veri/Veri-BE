package org.goorm.veri.veribe.domain.book.entity.service;

import org.goorm.veri.veribe.domain.book.entity.dtos.book.BookResponse;

import java.util.List;

public interface BookService {

    Long addBook(String title, String image, String author, String publisher, String isbn);

    List<BookResponse> searchBook(String query);
}
