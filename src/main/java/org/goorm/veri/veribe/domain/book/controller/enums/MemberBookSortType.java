package org.goorm.veri.veribe.domain.book.controller.enums;

import lombok.Getter;
import org.goorm.veri.veribe.domain.book.exception.MemberBookException;
import org.springframework.data.domain.Sort;

import static org.goorm.veri.veribe.domain.book.exception.MemberBookErrorCode.*;

@Getter
public enum MemberBookSortType {
    NEWEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST(Sort.by(Sort.Direction.ASC, "createdAt")),
    SCORE(Sort.by(Sort.Direction.DESC, "score"));

    private final Sort sort;

    MemberBookSortType(Sort sort) {
        this.sort = sort;
    }

    public static MemberBookSortType from(String value) {
        return switch (value.toLowerCase()) {
            case "newest" -> NEWEST;
            case "oldest" -> OLDEST;
            case "score" -> SCORE;
            default -> throw new MemberBookException(BAD_REQUEST);
        };
    }
}
