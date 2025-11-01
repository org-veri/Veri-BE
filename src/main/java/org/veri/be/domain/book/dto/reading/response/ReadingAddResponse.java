package org.veri.be.domain.book.dto.reading.response;

import java.time.LocalDateTime;

public record ReadingAddResponse (
        Long memberBookId,
        LocalDateTime createdAt
){}
