package org.goorm.veri.veribe.domain.book.dto.memberBook;

import org.springframework.data.domain.Page;

import java.util.List;

public record MemberBookListResponse(
        List<MemberBookResponse> memberBooks,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public MemberBookListResponse(Page<MemberBookResponse> pageData) {
        this(
                pageData.getContent(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
