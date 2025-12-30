package org.veri.be.book.dto.book;

import org.springframework.data.domain.Page;

import java.util.List;

public record BookPopularListResponse (
        List<BookPopularResponse> books,
        int page,
        int size,
        long totalElements,
        int totalPages
){
    public BookPopularListResponse (Page<BookPopularResponse> pageData) {
        this(
                pageData.getContent(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
