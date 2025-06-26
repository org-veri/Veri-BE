package org.goorm.veri.veribe.domain.book.dtos.memberBook;

import org.goorm.veri.veribe.domain.book.dtos.book.BookRequest;

public record MemberBookAddRequest (
        Long memberId,
        BookRequest bookRequest) {}
