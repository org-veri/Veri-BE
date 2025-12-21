package org.veri.be.domain.post.controller.enums;

import lombok.Getter;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.lib.exception.http.BadRequestException;
import org.springframework.data.domain.Sort;

@Getter
public enum PostSortType {
    NEWEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST(Sort.by(Sort.Direction.ASC, "createdAt"));

    private final Sort sort;

    PostSortType(Sort sort) {
        this.sort = sort;
    }

    public static PostSortType from(String value) {
        return switch (value.toLowerCase()) {
            case "newest" -> NEWEST;
            case "oldest" -> OLDEST;
            default -> throw new BadRequestException(CardErrorInfo.BAD_REQUEST);
        };
    }
}
