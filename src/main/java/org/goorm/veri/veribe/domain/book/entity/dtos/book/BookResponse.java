package org.goorm.veri.veribe.domain.book.entity.dtos.book;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookResponse {
    private String title;
    private String author;
    private String imageUrl;
}
