package org.veri.be.book.controller.enums;

import lombok.Getter;
import org.veri.be.book.exception.BookErrorCode;
import org.springframework.data.domain.Sort;
import org.veri.be.lib.exception.ApplicationException;

@Getter
public enum ReadingSortType {
    NEWEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST(Sort.by(Sort.Direction.ASC, "createdAt")),
    SCORE(Sort.by(Sort.Direction.DESC, "score"));

    private final Sort sort;

    ReadingSortType(Sort sort) {
        this.sort = sort;
    }

    public static ReadingSortType from(String value) {
        return switch (value.toLowerCase()) {
            case "newest" -> NEWEST;
            case "oldest" -> OLDEST;
            case "score" -> SCORE;
            default -> throw ApplicationException.of(BookErrorCode.BAD_REQUEST);
        };
    }
}
