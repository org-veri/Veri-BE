package org.veri.be.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.controller.enums.ReadingSortType;
import org.veri.be.book.dto.book.BookPopularResponse;
import org.veri.be.book.dto.reading.ReadingConverter;
import org.veri.be.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.book.dto.reading.response.ReadingResponse;
import org.veri.be.book.entity.Reading;
import org.veri.be.book.entity.enums.ReadingStatus;
import org.veri.be.book.repository.dto.BookPopularQueryResult;
import org.veri.be.book.repository.dto.ReadingQueryResult;
import org.veri.be.book.exception.BookErrorCode;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.member.auth.context.CurrentMemberAccessor;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.veri.be.book.entity.enums.ReadingStatus.DONE;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookshelfQueryService {

    private final ReadingRepository readingRepository;
    private final ReadingConverter readingConverter;
    private final CurrentMemberAccessor currentMemberAccessor;
    private final Clock clock;

    public Page<ReadingResponse> searchAllReadingOfMember(
            Long memberId,
            List<ReadingStatus> statuses,
            int page, int size, ReadingSortType sortType
    ) {
        Pageable pageRequest = PageRequest.of(page, size, sortType.getSort());

        return readingRepository.findReadingPage(
                memberId,
                statuses,
                pageRequest
        ).map(this::toReadingResponse);
    }

    public ReadingDetailResponse searchDetail(Long memberBookId) {
        Reading reading = readingRepository.findByIdWithBook(memberBookId)
                .orElseThrow(() -> ApplicationException.of(BookErrorCode.BAD_REQUEST));

        if (!reading.isPublic() && !reading.authorizeMember(currentMemberAccessor.getMemberOrThrow().getId())) {
            throw ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        return readingConverter.toReadingDetailResponse(reading);
    }

    public Page<BookPopularResponse> searchWeeklyPopular(int page, int size) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime startOfNextWeek = startOfWeek.plusWeeks(1);

        Pageable pageRequest = PageRequest.of(page, size);

        return readingRepository.findMostPopularBook(startOfWeek, startOfNextWeek, pageRequest)
                .map(this::toBookPopularResponse);
    }

    public int searchMyReadingDoneCount(Long memberId) {
        return readingRepository.countByStatusAndMember(DONE, memberId);
    }

    public Long searchByTitleAndAuthor(Long memberId, String title, String author) {
        Optional<Reading> memberBookOPT = readingRepository.findByAuthorAndTitle(memberId, title, author);

        return memberBookOPT.map(Reading::getId).orElse(null);
    }

    public Reading getReadingById(Long readingId) {
        return findReadingOrThrow(readingId);
    }

    public Reading getReadingByIdOrInvalid(Long readingId) {
        return readingRepository.findById(readingId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.INVALID_REQUEST));
    }

    private Reading findReadingOrThrow(Long readingId) {
        return readingRepository.findById(readingId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND));
    }

    private ReadingResponse toReadingResponse(ReadingQueryResult result) {
        return new ReadingResponse(
                result.bookId(),
                result.memberBookId(),
                result.title(),
                result.author(),
                result.imageUrl(),
                result.score(),
                result.startedAt(),
                result.status(),
                result.isPublic()
        );
    }

    private BookPopularResponse toBookPopularResponse(BookPopularQueryResult result) {
        return new BookPopularResponse(
                result.image(),
                result.title(),
                result.author(),
                result.publisher(),
                result.isbn()
        );
    }
}
