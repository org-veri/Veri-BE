package org.veri.be.book.dto.reading.response;

import java.time.LocalDateTime;

public record ReadingAddResponse (
        Long memberBookId,
        LocalDateTime createdAt
){}
