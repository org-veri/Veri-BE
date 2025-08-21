package org.goorm.veri.veribe.domain.book.dto.reading;

import java.time.LocalDateTime;

public record ReadingAddResponse (
        Long memberBookId,
        LocalDateTime createdAt
){}
