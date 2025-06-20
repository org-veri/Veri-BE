package org.goorm.veri.veribe.domain.book.entity.dtos.memberBook;

import java.time.LocalDateTime;

public record MemberBookAddRequest (
    Long bookId,
    Long memberId,
    Double score,
    LocalDateTime startedAt,
    LocalDateTime endedAt) {}
