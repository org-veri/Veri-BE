package org.goorm.veri.veribe.domain.book.dtos.memberBook;

import lombok.Builder;
import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;

import java.util.List;

@Builder
public record MemberBookDetailResponse (
        Long bookId,
        String title,
        String author,
        String imageUrl,
        BookStatus status,
        Double score,
        List<String> cardUrls
        ) {}
