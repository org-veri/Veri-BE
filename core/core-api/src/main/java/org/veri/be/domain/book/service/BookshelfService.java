package org.veri.be.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.book.dto.reading.response.ReadingVisibilityUpdateResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.exception.BookErrorCode;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.veri.be.domain.book.entity.enums.ReadingStatus.NOT_START;

@Service
@Transactional
@RequiredArgsConstructor
public class BookshelfService {

    private final ReadingRepository readingRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional
    public Reading addToBookshelf(Long memberId, Long bookId, boolean isPublic) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ApplicationException.of(BookErrorCode.BAD_REQUEST));

        //Reading 중복 저장 방지 로직 추가 -> 기존 책을 응답
        Optional<Reading> findReading = readingRepository.findByMemberAndBook(memberId, bookId);
        if (findReading.isPresent()) {
            return findReading.get();
        }

        Member member = memberRepository.getReferenceById(memberId);
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

    @Transactional
    public void modifyBook(Long memberId, Double score, LocalDateTime startedAt, LocalDateTime endedAt, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(memberId);

        reading.updateProgress(score, startedAt, endedAt);
        readingRepository.save(reading);
    }

    @Transactional
    public void rateScore(Long memberId, Double score, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(memberId);

        reading.updateScore(score);
        readingRepository.save(reading);
    }

    @Transactional
    public void readStart(Long memberId, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(memberId);

        reading.start(clock);
        readingRepository.save(reading);
    }

    @Transactional
    public void readOver(Long memberId, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(memberId);

        reading.finish(clock);
        readingRepository.save(reading);
    }

    @Transactional
    public void deleteBook(Long memberId, Long memberBookId) {
        Reading reading = getReadingById(memberBookId);
        reading.authorizeOrThrow(memberId);

        readingRepository.delete(reading);
    }

    @Transactional
    public ReadingVisibilityUpdateResponse modifyVisibility(Long memberId, Long readingId, boolean isPublic) {
        Reading reading = getReadingById(readingId);
        reading.authorizeOrThrow(memberId);

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
}
