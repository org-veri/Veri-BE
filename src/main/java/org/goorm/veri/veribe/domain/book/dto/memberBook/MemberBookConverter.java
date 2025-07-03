package org.goorm.veri.veribe.domain.book.dto.memberBook;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;

import java.util.List;

public class MemberBookConverter {

    public static MemberBookDetailResponse toMemberBookDetailResponse(MemberBook memberBook) {

        List<CardSummaries> summaries = memberBook.getCards().stream()
                                        .map(card -> new CardSummaries(card.getId(), card.getImage()))
                                        .toList();

        return MemberBookDetailResponse.builder()
                .memberBookId(memberBook.getId())
                .bookId(memberBook.getBook().getId())
                .title(memberBook.getBook().getTitle())
                .imageUrl(memberBook.getBook().getImage())
                .status(memberBook.getStatus())
                .score(memberBook.getScore())
                .author(memberBook.getBook().getAuthor())
                .startedAt(memberBook.getStartedAt())
                .cardSummaries(summaries)
                .build();
    }
}
