package org.goorm.veri.veribe.domain.book.dto.memberBook;

import java.time.LocalDateTime;

public record MemberBookAddResponse (
        Long memberBookId,
        LocalDateTime createdAt
){}
