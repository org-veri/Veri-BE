package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.card.repository.dto.CardListItem;
import org.springframework.data.domain.Page;

import java.util.List;

public record CardListResponse(
        List<CardListItem> cards,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public CardListResponse(Page<CardListItem> pageData) {
        this(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
