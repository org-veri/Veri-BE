package org.goorm.veri.veribe.domain.book.dtos.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
@Getter
public class NaverBookItem {

    @JsonProperty("title")
    private String title;

    @JsonProperty("link")
    private String link;

    @JsonProperty("image")
    private String image;

    @JsonProperty("author")
    private String author;

    @JsonProperty("price")
    private String price;

    @JsonProperty("discount")
    private String discount;

    @JsonProperty("publisher")
    private String publisher;

    @JsonProperty("pubdate")
    private String pubdate;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("description")
    private String description;

}
