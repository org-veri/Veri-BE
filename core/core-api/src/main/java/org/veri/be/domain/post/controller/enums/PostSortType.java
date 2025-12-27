package org.veri.be.domain.post.controller.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

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
            default -> throw ApplicationException.of(CommonErrorCode.INVALID_REQUEST);
        };
    }
}
