package org.goorm.veri.veribe.domain.card.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CardListItem {
    private Long cardId;
    private String content;
    private String image;
    private LocalDateTime created;
}
