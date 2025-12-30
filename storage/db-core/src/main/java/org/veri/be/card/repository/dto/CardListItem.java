package org.veri.be.card.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CardListItem {
    private Long cardId;
    private String bookTitle;
    private String content;
    private String image;
    private LocalDateTime created;
    private boolean isPublic;
}
