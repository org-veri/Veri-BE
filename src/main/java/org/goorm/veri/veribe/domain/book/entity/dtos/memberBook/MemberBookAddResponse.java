package org.goorm.veri.veribe.domain.book.entity.dtos.memberBook;

import java.time.LocalDateTime;

public record MemberBookAddResponse (
        Long memberBookId,
        LocalDateTime createdAt
){}
