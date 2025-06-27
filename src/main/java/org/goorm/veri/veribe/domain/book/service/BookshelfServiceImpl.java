package org.goorm.veri.veribe.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.entity.Book;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookConverter;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookResponse;
import org.goorm.veri.veribe.domain.book.exception.MemberBookException;
import org.goorm.veri.veribe.domain.book.repository.BookRepository;
import org.goorm.veri.veribe.domain.book.repository.MemberBookRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.goorm.veri.veribe.domain.book.entity.enums.BookStatus.*;
import static org.goorm.veri.veribe.domain.book.exception.MemberBookErrorCode.BAD_REQUEST;

@Service
@Transactional
@RequiredArgsConstructor
public class BookshelfServiceImpl implements BookshelfService {

    private final MemberBookRepository memberBookRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Override
    public MemberBook addToBookshelf(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        MemberBook memberBook = MemberBook.builder()
                .member(member)
                .book(book)
                .score(null)
                .startedAt(null)
                .endedAt(null)
                .status(NOT_START)
                .cards(new ArrayList<>())
                .build();

        return memberBookRepository.save(memberBook);
    }

    @Override
    public List<MemberBookResponse> searchAll(Long memberId) {
        List<MemberBook> result = memberBookRepository.findAllByMember_Id(memberId);

        List<MemberBookResponse> dtos = new ArrayList<>();
        for (MemberBook memberBook : result) {
            dtos.add(MemberBookConverter.toMemberBookResponse(memberBook));
        }

        return dtos;
    }

    @Override
    public MemberBookDetailResponse searchDetail(Long memberBookId) {
        MemberBook memberBook = memberBookRepository.findByIdWithCardsAndBook(memberBookId)
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        MemberBookDetailResponse dto = MemberBookConverter.toMemberBookDetailResponse(memberBook);

        return dto;
    }

    @Override
    public void rateScore(Double score, Long memberBookId) {
        MemberBook memberBook = memberBookRepository.findById(memberBookId)
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        MemberBook updated = MemberBook.builder()
                .id(memberBook.getId())
                .member(memberBook.getMember())
                .book(memberBook.getBook())
                .score(score)
                .startedAt(memberBook.getStartedAt())
                .endedAt(memberBook.getEndedAt())
                .status(memberBook.getStatus())
                .cards(memberBook.getCards())
                .build();

        memberBookRepository.save(updated);
    }

    @Override
    public void readStart(Long memberBookId) {
        MemberBook memberBook = memberBookRepository.findById(memberBookId)
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedTime = LocalDateTime.of(now.getYear(),
                now.getMonth(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                0,
                0);

        MemberBook updated = MemberBook.builder()
                .id(memberBook.getId())
                .member(memberBook.getMember())
                .book(memberBook.getBook())
                .score(memberBook.getScore())
                .startedAt(LocalDateTime.now())
                .endedAt(startedTime)
                .status(READING)
                .cards(memberBook.getCards())
                .build();

        memberBookRepository.save(updated);
    }

    @Override
    public void readOver(Long memberBookId) {
        MemberBook memberBook = memberBookRepository.findById(memberBookId)
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endedTime = LocalDateTime.of(now.getYear(),
                now.getMonth(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                0,
                0);

        MemberBook updated = MemberBook.builder()
                .id(memberBook.getId())
                .member(memberBook.getMember())
                .book(memberBook.getBook())
                .score(memberBook.getScore())
                .startedAt(memberBook.getStartedAt())
                .endedAt(endedTime)
                .status(DONE)
                .cards(memberBook.getCards())
                .build();

        memberBookRepository.save(updated);
    }

    @Override
    public void deleteBook(Long memberBookId) {
        MemberBook memberBook = memberBookRepository.findById(memberBookId)
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        Book book = bookRepository.findById(memberBook.getBook().getId())
                .orElseThrow(() -> new MemberBookException(BAD_REQUEST));

        memberBookRepository.delete(memberBook);
        bookRepository.delete(book);
    }

}
