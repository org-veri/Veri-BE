package org.goorm.veri.veribe.domain.card.controller.enums;

import lombok.Getter;
import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
import org.goorm.veri.veribe.domain.card.exception.CardException;
import org.springframework.data.domain.Sort;

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
            default -> throw new CardException(CardErrorCode.BAD_REQUEST);
        };
    }
}
