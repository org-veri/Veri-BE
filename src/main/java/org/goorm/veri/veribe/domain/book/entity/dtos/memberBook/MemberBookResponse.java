package org.goorm.veri.veribe.domain.book.entity.dtos.memberBook;

import lombok.Builder;

import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;

@Builder
public record MemberBookResponse (
        Long bookId,
        String title,
        String author,
        String imageUrl,
        BookStatus status)
{}

