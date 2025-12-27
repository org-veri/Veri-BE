package org.veri.be.domain.book.dto.book;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;

@Getter
public class NaverBookResponse {

    @JsonProperty("lastBuildDate")
    private String lastBuildDate;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("start")
    private Integer start;

    @JsonProperty("display")
    private Integer display;

    @JsonProperty("items")
    private List<NaverBookItem> items;
}
