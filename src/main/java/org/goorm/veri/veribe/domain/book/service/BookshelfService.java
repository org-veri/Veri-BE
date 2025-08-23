package org.goorm.veri.veribe.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.book.controller.enums.ReadingSortType;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.reading.ReadingConverter;
import org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingDetailResponse;
import org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingResponse;
import org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingVisibilityUpdateResponse;
import org.goorm.veri.veribe.domain.book.entity.Book;
import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;
import org.goorm.veri.veribe.domain.book.exception.BookErrorInfo;
import org.goorm.veri.veribe.domain.book.repository.BookRepository;
import org.goorm.veri.veribe.domain.book.repository.ReadingRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.exception.http.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

import static org.goorm.veri.veribe.domain.book.entity.enums.BookStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class BookshelfService {

    private final ReadingRepository readingRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Reading addToBookshelf(Member member, Long bookId, boolean isPublic) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BadRequestException(BookErrorInfo.BAD_REQUEST));

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
    public Page<ReadingResponse> searchAllReadingOfMember(Long memberId, int page, int size, ReadingSortType sortType) {
        Pageable pageRequest = PageRequest.of(page, size, sortType.getSort());

        Page<ReadingResponse> responses = readingRepository.findReadingPage(memberId, pageRequest);

        return responses;
    }

    @Transactional(readOnly = true)
    public ReadingDetailResponse searchDetail(Long memberBookId) {
        Reading reading = readingRepository.findByIdWithCardsAndBook(memberBookId)
                .orElseThrow(() -> new BadRequestException(BookErrorInfo.BAD_REQUEST));

        if (!reading.getIsPublic()) {
            reading.authorizeMember(AuthUtil.getCurrentMember().getId());
        }

        ReadingDetailResponse dto = ReadingConverter.toReadingDetailResponse(reading);

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<BookPopularResponse> searchPopular(int page, int size) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime startOfNextWeek = startOfWeek.plusWeeks(1);

        Pageable pageRequest = PageRequest.of(page, size);

        Page<BookPopularResponse> responses = readingRepository.findMostPopularBook(startOfWeek, startOfNextWeek, pageRequest);

        return responses;
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
    public void modifyBook(Double score, LocalDateTime startedAt, LocalDateTime endedAt, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeMember(AuthUtil.getCurrentMember().getId());

        BookStatus updateStatus = decideStatus(startedAt, endedAt);

        Reading updated = reading.toBuilder()
                .score(score)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .status(updateStatus)
                .build();

        readingRepository.save(updated);
    }

    private BookStatus decideStatus(LocalDateTime updateStart, LocalDateTime updateEnd) {
        if (updateEnd != null) { //독서 완료 시간이 존재 시 DONE
            return DONE;
        }

        if (updateStart != null) { //독서 완료 시간은 null, 독서 시작 시간은 존재시 READING
            return READING;
        }

        return NOT_START; //그 이외는 독서 시작 & 완료 모두 null 이므로 NOT_START
    }

    @Transactional
    public void rateScore(Double score, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeMember(AuthUtil.getCurrentMember().getId());

        Reading updated = reading.toBuilder().score(score).build();

        readingRepository.save(updated);
    }

    @Transactional
    public void readStart(Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeMember(AuthUtil.getCurrentMember().getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedTime = LocalDateTime.of(now.getYear(),
                now.getMonth(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                0,
                0);

        Reading updated = reading.toBuilder()
                .startedAt(startedTime)
                .status(READING)
                .build();

        readingRepository.save(updated);
    }

    @Transactional
    public void readOver(Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeMember(AuthUtil.getCurrentMember().getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endedTime = LocalDateTime.of(now.getYear(),
                now.getMonth(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                0,
                0);

        Reading updated = reading.toBuilder()
                .endedAt(endedTime)
                .status(DONE)
                .build();

        readingRepository.save(updated);
    }

    @Transactional
    public void deleteBook(Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeMember(AuthUtil.getCurrentMember().getId());

        readingRepository.delete(reading);
    }

    @Transactional
    public ReadingVisibilityUpdateResponse modifyVisibility(Long readingId, boolean isPublic) {
        Reading reading = getReadingById(readingId);
        reading.authorizeMember(AuthUtil.getCurrentMember().getId());

        if (isPublic) {
            reading.setPublic();
        } else {
            reading.setPrivate();
        }

        readingRepository.save(reading);
        return new ReadingVisibilityUpdateResponse(reading.getId(), reading.getIsPublic());
    }

    private Reading getReadingById(Long readingId) {
        return readingRepository.findById(readingId)
                .orElseThrow(() -> new BadRequestException(BookErrorInfo.BAD_REQUEST));
    }
}
