package org.goorm.veri.veribe.domain.book.dto.memberBook;

import lombok.Builder;
import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MemberBookDetailResponse(
        // Todo. 작성자 추가
        // Todo. 책 ID 추가해서 내책장으로 가져가기?
        Long memberBookId,
        String title,
        String author,
        String imageUrl,
        BookStatus status,
        Double score,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<CardSummaries> cardSummaries
) {
}
