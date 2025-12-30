package org.veri.be.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.dto.reading.response.ReadingVisibilityUpdateResponse;
import org.veri.be.book.entity.Book;
import org.veri.be.book.entity.Reading;
import org.veri.be.book.exception.BookErrorCode;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.member.entity.Member;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.veri.be.book.entity.enums.ReadingStatus.NOT_START;

@Service
@Transactional
@RequiredArgsConstructor
public class BookshelfCommandService {

    private final ReadingRepository readingRepository;
    private final BookRepository bookRepository;
    private final ReadingCardSummaryProvider readingCardSummaryProvider;
    private final Clock clock;

    public Reading addToBookshelf(Member member, Long bookId, boolean isPublic) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ApplicationException.of(BookErrorCode.BAD_REQUEST));

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
                .isPublic(isPublic)
                .build();

        return readingRepository.save(reading);
    }

    public void modifyBook(Member member, Double score, LocalDateTime startedAt, LocalDateTime endedAt, Long memberBookId) {
        Reading reading = findReadingOrThrow(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.updateProgress(score, startedAt, endedAt);
        readingRepository.save(reading);
    }

    public void rateScore(Member member, Double score, Long memberBookId) {
        Reading reading = findReadingOrThrow(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.updateScore(score);
        readingRepository.save(reading);
    }

    public void readStart(Member member, Long memberBookId) {
        Reading reading = findReadingOrThrow(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.start(clock);
        readingRepository.save(reading);
    }

    public void readOver(Member member, Long memberBookId) {
        Reading reading = findReadingOrThrow(memberBookId);
        reading.authorizeOrThrow(member.getId());

        reading.finish(clock);
        readingRepository.save(reading);
    }

    public void deleteBook(Member member, Long memberBookId) {
        Reading reading = findReadingOrThrow(memberBookId);
        reading.authorizeOrThrow(member.getId());

        readingRepository.delete(reading);
    }

    public ReadingVisibilityUpdateResponse modifyVisibility(Member member, Long readingId, boolean isPublic) {
        Reading reading = findReadingOrThrow(readingId);
        reading.authorizeOrThrow(member.getId());

        if (isPublic) {
            reading.setPublic();
        } else {
            reading.setPrivate();
            readingCardSummaryProvider.setCardsPrivate(readingId);
        }

        readingRepository.save(reading);
        return new ReadingVisibilityUpdateResponse(reading.getId(), reading.isPublic());
    }

    private Reading findReadingOrThrow(Long readingId) {
        return readingRepository.findById(readingId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND));
    }
}
