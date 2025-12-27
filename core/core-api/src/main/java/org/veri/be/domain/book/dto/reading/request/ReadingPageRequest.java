package org.veri.be.domain.book.dto.reading.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.veri.be.domain.book.controller.enums.ReadingSortType;
import org.veri.be.domain.book.entity.enums.ReadingStatus;

import java.util.List;

@Setter
@Getter
public class ReadingPageRequest {
    @Min(1)
    private int page = 1;

    @Min(1)
    private int size = 10;

    private List<ReadingStatus> statuses = List.of(ReadingStatus.values()); // all

    private ReadingSortType sortType = ReadingSortType.NEWEST;
}
