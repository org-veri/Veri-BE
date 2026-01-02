package org.veri.be.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.book.controller.enums.ReadingSortType;
import org.veri.be.domain.book.dto.book.BookPopularResponse;
import org.veri.be.domain.book.dto.reading.ReadingConverter;
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingVisibilityUpdateResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.domain.book.exception.BookErrorCode;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.book.repository.dto.BookPopularQueryResult;
import org.veri.be.domain.book.repository.dto.ReadingQueryResult;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.context.CurrentMemberAccessor;
import org.veri.be.global.auth.context.CurrentMemberInfo;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.veri.be.domain.book.entity.enums.ReadingStatus.DONE;
import static org.veri.be.domain.book.entity.enums.ReadingStatus.NOT_START;

@Service
@Transactional
@RequiredArgsConstructor
public class BookshelfService {

    private final ReadingRepository readingRepository;
    private final BookRepository bookRepository;
    private final ReadingConverter readingConverter;
    private final CurrentMemberAccessor currentMemberAccessor;
    private final Clock clock;

    @Transactional
    public Reading addToBookshelf(Member member, Long bookId, boolean isPublic) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ApplicationException.of(BookErrorCode.BAD_REQUEST));

        //Reading 중복 저장 방지 로직 추가 -> 기존 책을 응답
        Optional<Reading> findReading = readingRepository.findByMemberAndBook(member.getId(), bookId);
        if (findReading.isPresent()) {
            return findReading.get();
        }

        Reading reading = Reading.builder()
                .member(member)
                .book(book)
                .score(null)
                .startedAt(null)
                .endedAt(null)
                .status(NOT_START)
                .cards(new ArrayList<>())
                .isPublic(isPublic)
                .build();

        return readingRepository.save(reading);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public ReadingDetailResponse searchDetail(Long memberBookId) {
        Reading reading = readingRepository.findByIdWithCardsAndBook(memberBookId)
                .orElseThrow(() -> ApplicationException.of(BookErrorCode.BAD_REQUEST));

        CurrentMemberInfo memberInfo = currentMemberAccessor.getMemberInfoOrThrow();
        if (!reading.isPublic() && !reading.authorizeMember(memberInfo.id())) {
            throw ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        return readingConverter.toReadingDetailResponse(reading);
    }

    @Transactional(readOnly = true)
    public Page<BookPopularResponse> searchWeeklyPopular(int page, int size) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime startOfNextWeek = startOfWeek.plusWeeks(1);

        Pageable pageRequest = PageRequest.of(page, size);

        return readingRepository.findMostPopularBook(startOfWeek, startOfNextWeek, pageRequest)
                .map(this::toBookPopularResponse);
    }

    @Transactional(readOnly = true)
    public int searchMyReadingDoneCount(Long memberId) {
        return readingRepository.countByStatusAndMember(DONE, memberId);
    }

    @Transactional(readOnly = true)
    public Long searchByTitleAndAuthor(Long memberId, String title, String author) {
        Optional<Reading> memberBookOPT = readingRepository.findByAuthorAndTitle(memberId, title, author);

        return memberBookOPT.map(Reading::getId).orElse(null);
    }

    @Transactional
    public void modifyBook(Member member, Double score, LocalDateTime startedAt, LocalDateTime endedAt, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.updateProgress(score, startedAt, endedAt);
        readingRepository.save(reading);
    }

    @Transactional
    public void rateScore(Member member, Double score, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.updateScore(score);
        readingRepository.save(reading);
    }

    @Transactional
    public void readStart(Member member, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.start(clock);
        readingRepository.save(reading);
    }

    @Transactional
    public void readOver(Member member, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.finish(clock);
        readingRepository.save(reading);
    }

    @Transactional
    public void deleteBook(Member member, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(member.getId());

        readingRepository.delete(reading);
    }

    @Transactional
    public ReadingVisibilityUpdateResponse modifyVisibility(Member member, Long readingId, boolean isPublic) {
        Reading reading = getReadingById(readingId);
        reading.authorizeOrThrow(member.getId());

        if (isPublic) {
            reading.setPublic();
        } else {
            reading.setPrivate();
        }

        readingRepository.save(reading);
        return new ReadingVisibilityUpdateResponse(reading.getId(), reading.isPublic());
    }

    private Reading getReadingById(Long readingId) {
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
