package org.veri.be.domain.card.controller.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

@Getter
public enum CardSortType {
    NEWEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST(Sort.by(Sort.Direction.ASC, "createdAt"));

    private final Sort sort;

    CardSortType(Sort sort) {
        this.sort = sort;
    }

    public static CardSortType from(String value) {
        return switch (value.toLowerCase()) {
            case "newest" -> NEWEST;
            case "oldest" -> OLDEST;
            default -> throw ApplicationException.of(CommonErrorCode.INVALID_REQUEST);
        };
    }
}
