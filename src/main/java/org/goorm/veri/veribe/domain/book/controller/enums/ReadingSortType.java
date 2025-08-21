package org.goorm.veri.veribe.domain.book.controller.enums;

import lombok.Getter;
import org.goorm.veri.veribe.domain.book.exception.BookErrorInfo;
import org.goorm.veri.veribe.global.exception.http.BadRequestException;
import org.springframework.data.domain.Sort;

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
            default -> throw new BadRequestException(BookErrorInfo.BAD_REQUEST);
        };
    }
}
