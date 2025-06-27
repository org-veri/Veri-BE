package org.goorm.veri.veribe.domain.book.dto.memberBook;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;

import java.util.List;

public class MemberBookConverter {

    public static MemberBookResponse toMemberBookResponse(MemberBook memberBook) {
        return MemberBookResponse.builder()
                .bookId(memberBook.getBook().getId())
                .title(memberBook.getBook().getTitle())
                .imageUrl(memberBook.getBook().getImage())
                .status(memberBook.getStatus())
                .author(memberBook.getBook().getAuthor())
                .score(memberBook.getScore())
                .build();
    }

    public static MemberBookDetailResponse toMemberBookDetailResponse(MemberBook memberBook) {

        List<CardSummaries> summaries = memberBook.getCards().stream()
                                        .map(card -> new CardSummaries(card.getId(), card.getImage()))
                                        .toList();

        return MemberBookDetailResponse.builder()
                .bookId(memberBook.getBook().getId())
                .title(memberBook.getBook().getTitle())
                .imageUrl(memberBook.getBook().getImage())
                .status(memberBook.getStatus())
                .score(memberBook.getScore())
                .author(memberBook.getBook().getAuthor())
                .cardSummaries(summaries)
                .build();
    }
}
