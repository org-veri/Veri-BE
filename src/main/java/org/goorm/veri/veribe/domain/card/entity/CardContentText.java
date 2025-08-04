package org.goorm.veri.veribe.domain.card.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Embeddable
public class CardContentText {

    private String text;
    private String font;
    private String color;
    private String size; // px

    public static CardContentText ofDefault(String text) {
        CardContentText cardContentText = new CardContentText();
        cardContentText.text = text;
        cardContentText.font = "Arial";
        cardContentText.color = "#000000";
        cardContentText.size = "16";
        return cardContentText;
    }
}
