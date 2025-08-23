package org.goorm.veri.veribe.domain.book.dto.reading.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record ReadingListResponse(
        List<ReadingResponse> memberBooks,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public ReadingListResponse(Page<ReadingResponse> pageData) {
        this(
                pageData.getContent(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
