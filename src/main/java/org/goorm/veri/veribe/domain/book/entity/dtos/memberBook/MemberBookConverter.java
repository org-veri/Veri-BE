package org.goorm.veri.veribe.domain.book.entity.dtos.memberBook;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;

public class MemberBookConverter {

    public static MemberBookResponse toMemberBookResponse(MemberBook memberBook) {
        return MemberBookResponse.builder()
                .bookId(memberBook.getBook().getId())
                .title(memberBook.getBook().getTitle())
                .imageUrl(memberBook.getBook().getImage())
                .status(memberBook.getStatus())
                .author(memberBook.getBook().getAuthor())
                .build();
    }
}
