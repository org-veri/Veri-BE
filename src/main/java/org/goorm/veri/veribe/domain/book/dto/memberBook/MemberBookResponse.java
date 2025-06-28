package org.goorm.veri.veribe.domain.book.dto.memberBook;

import lombok.Builder;

import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;

import java.time.LocalDateTime;

@Builder
public record MemberBookResponse (
        Long bookId,
        String title,
        String author,
        String imageUrl,
        Double score,
        LocalDateTime startedAt,
        BookStatus status)
{}

