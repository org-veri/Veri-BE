package org.goorm.veri.veribe.domain.book.dtos.memberBook;

import org.goorm.veri.veribe.domain.book.dtos.book.BookRequest;

import java.time.LocalDateTime;

public record MemberBookAddRequest (
        Long memberId,
        Double score,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        BookRequest bookRequest) {}
