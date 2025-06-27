package org.goorm.veri.veribe.domain.book.dtos.memberBook;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.card.entity.Card;

import java.util.ArrayList;
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

        List<String> cardUrls = new ArrayList<>();

        List<Card> cards = memberBook.getCards();

        for (Card card : cards) {
            cardUrls.add(card.getImage());
        }

        return MemberBookDetailResponse.builder()
                .bookId(memberBook.getBook().getId())
                .title(memberBook.getBook().getTitle())
                .imageUrl(memberBook.getBook().getImage())
                .status(memberBook.getStatus())
                .score(memberBook.getScore())
                .author(memberBook.getBook().getAuthor())
                .cardUrls(cardUrls)
                .build();
    }
}
