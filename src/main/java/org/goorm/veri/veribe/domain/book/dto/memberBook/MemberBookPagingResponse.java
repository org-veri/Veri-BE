package org.goorm.veri.veribe.domain.book.dto.memberBook;

import java.util.List;

public record MemberBookPagingResponse(
        List<MemberBookResponse> memberBooks,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
