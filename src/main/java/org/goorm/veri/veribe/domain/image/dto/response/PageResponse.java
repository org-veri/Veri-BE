package org.goorm.veri.veribe.domain.image.dto.response;

import org.springframework.data.domain.Pageable;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        // 전달 받은 page가 0-based index이므로 다시 1-based index로 보정하여 프론트에 전달.
        return new PageResponse<>(content, page + 1, size, totalElements, totalPages);
    }

    public static <T> PageResponse<T> empty(Pageable pageable) {
        return new PageResponse<>(List.of(), pageable.getPageNumber() + 1, pageable.getPageSize(), 0, 0);
    }
}