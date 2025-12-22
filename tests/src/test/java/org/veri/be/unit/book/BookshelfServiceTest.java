package org.veri.be.unit.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.veri.be.domain.book.controller.enums.ReadingSortType;
import org.veri.be.domain.book.dto.book.BookPopularResponse;
import org.veri.be.domain.book.dto.reading.ReadingConverter;
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingVisibilityUpdateResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.domain.book.exception.BookErrorCode;
import org.veri.be.domain.book.exception.ReadingErrorCode;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.book.service.BookshelfService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.CurrentMemberAccessor;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class BookshelfServiceTest {

    @Mock
    ReadingRepository readingRepository;

    @Mock
    BookRepository bookRepository;

    @Mock
    ReadingConverter readingConverter;

    @Mock
    CurrentMemberAccessor currentMemberAccessor;

    Clock fixedClock = Clock.fixed(Instant.parse("2024-01-03T12:00:00Z"), ZoneId.of("UTC"));

    BookshelfService bookshelfService;

    @Captor
    ArgumentCaptor<Reading> readingCaptor;

    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    ArgumentCaptor<LocalDateTime> dateTimeCaptor;

    @BeforeEach
    void setUp() {
        bookshelfService = new BookshelfService(
                readingRepository,
                bookRepository,
                readingConverter,
                currentMemberAccessor,
                fixedClock
        );
    }

    @Nested
    @DisplayName("addToBookshelf")
    class AddToBookshelf {

        @Test
        @DisplayName("도서가 없으면 예외가 발생한다")
        void throwsWhenBookMissing() {
            Member member = member(1L, "member@test.com", "member");
            given(bookRepository.findById(1L)).willReturn(Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> bookshelfService.addToBookshelf(member, 1L, true),
                    BookErrorCode.BAD_REQUEST
            );
        }

        @Test
        @DisplayName("이미 등록된 독서가 있으면 기존 독서를 반환한다")
        void returnsExistingReading() {
            Member member = member(1L, "member@test.com", "member");
            Book book = book(1L);
            Reading reading = reading(10L, member, book, ReadingStatus.NOT_START, true);

            given(bookRepository.findById(1L)).willReturn(Optional.of(book));
            given(readingRepository.findByMemberAndBook(1L, 1L)).willReturn(Optional.of(reading));

            Reading result = bookshelfService.addToBookshelf(member, 1L, true);

            assertThat(result).isEqualTo(reading);
            verify(readingRepository, never()).save(any(Reading.class));
        }

        @Test
        @DisplayName("신규 독서를 저장한다")
        void savesNewReading() {
            Member member = member(1L, "member@test.com", "member");
            Book book = book(1L);

            given(bookRepository.findById(1L)).willReturn(Optional.of(book));
            given(readingRepository.findByMemberAndBook(1L, 1L)).willReturn(Optional.empty());
            given(readingRepository.save(any(Reading.class))).willAnswer(invocation -> invocation.getArgument(0));

            Reading result = bookshelfService.addToBookshelf(member, 1L, false);

            verify(readingRepository).save(readingCaptor.capture());
            Reading saved = readingCaptor.getValue();
            assertThat(saved.getMember()).isEqualTo(member);
            assertThat(saved.getBook()).isEqualTo(book);
            assertThat(saved.getStatus()).isEqualTo(ReadingStatus.NOT_START);
            assertThat(saved.getIsPublic()).isFalse();
            assertThat(result).isEqualTo(saved);
        }
    }

    @Nested
    @DisplayName("searchAllReadingOfMember")
    class SearchAllReadingOfMember {

        @Test
        @DisplayName("정렬 조건에 맞는 페이징 요청을 전달한다")
        void passesPagingWithSort() {
            given(readingRepository.findReadingPage(any(Long.class), any(List.class), any(Pageable.class)))
                    .willReturn(Page.empty());

            bookshelfService.searchAllReadingOfMember(1L, List.of(ReadingStatus.READING), 1, 20, ReadingSortType.SCORE);

            verify(readingRepository).findReadingPage(any(Long.class), any(List.class), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort()).isEqualTo(ReadingSortType.SCORE.getSort());
        }
    }

    @Nested
    @DisplayName("searchDetail")
    class SearchDetail {

        @Test
        @DisplayName("비공개 독서는 조회 권한을 확인한다")
        void checksAuthorizationWhenPrivate() {
            Member owner = member(1L, "owner@test.com", "owner");
            Member viewer = member(2L, "viewer@test.com", "viewer");
            Reading reading = reading(10L, owner, book(1L), ReadingStatus.READING, false);

            given(readingRepository.findByIdWithCardsAndBook(10L)).willReturn(Optional.of(reading));
            given(currentMemberAccessor.getMemberOrThrow()).willReturn(viewer);

            ExceptionAssertions.assertApplicationException(
                    () -> bookshelfService.searchDetail(10L),
                    ReadingErrorCode.FORBIDDEN
            );
        }

        @Test
        @DisplayName("공개 독서는 상세 응답을 반환한다")
        void returnsDetailForPublic() {
            Member owner = member(1L, "owner@test.com", "owner");
            Reading reading = reading(10L, owner, book(1L), ReadingStatus.READING, true);
            ReadingDetailResponse response = ReadingDetailResponse.builder()
                    .memberBookId(10L)
                    .build();

            given(readingRepository.findByIdWithCardsAndBook(10L)).willReturn(Optional.of(reading));
            given(readingConverter.toReadingDetailResponse(reading)).willReturn(response);

            ReadingDetailResponse result = bookshelfService.searchDetail(10L);

            assertThat(result).isEqualTo(response);
            verify(readingConverter).toReadingDetailResponse(reading);
        }
    }

    @Nested
    @DisplayName("searchWeeklyPopular")
    class SearchWeeklyPopular {

        @Test
        @DisplayName("주간 인기 도서를 조회한다")
        void queriesWeeklyPopular() {
            given(readingRepository.findMostPopularBook(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(new BookPopularResponse("img", "title", "author", "pub", "isbn"))));

            Page<BookPopularResponse> result = bookshelfService.searchWeeklyPopular(0, 10);

            verify(readingRepository).findMostPopularBook(dateTimeCaptor.capture(), dateTimeCaptor.capture(), any(Pageable.class));
            List<LocalDateTime> captured = dateTimeCaptor.getAllValues();
            assertThat(captured.get(0).getDayOfWeek().getValue()).isEqualTo(1);
            assertThat(captured.get(1)).isAfter(captured.get(0));
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("searchMyReadingDoneCount")
    class SearchMyReadingDoneCount {

        @Test
        @DisplayName("완독 개수를 반환한다")
        void returnsDoneCount() {
            given(readingRepository.countByStatusAndMember(ReadingStatus.DONE, 1L)).willReturn(2);

            int result = bookshelfService.searchMyReadingDoneCount(1L);

            assertThat(result).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("searchByTitleAndAuthor")
    class SearchByTitleAndAuthor {

        @Test
        @DisplayName("일치하는 독서가 없으면 null을 반환한다")
        void returnsNullWhenMissing() {
            given(readingRepository.findByAuthorAndTitle(1L, "title", "author")).willReturn(Optional.empty());

            Long result = bookshelfService.searchByTitleAndAuthor(1L, "title", "author");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("modifyBook")
    class ModifyBook {

        @Test
        @DisplayName("진행 정보를 수정한다")
        void updatesProgress() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = reading(10L, member, book(1L), ReadingStatus.NOT_START, true);
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading));

            bookshelfService.modifyBook(member, 4.0, LocalDateTime.now(), LocalDateTime.now(), 10L);

            verify(readingRepository).save(readingCaptor.capture());
            assertThat(readingCaptor.getValue().getStatus()).isEqualTo(ReadingStatus.DONE);
        }
    }

    @Nested
    @DisplayName("rateScore")
    class RateScore {

        @Test
        @DisplayName("평점을 수정한다")
        void updatesScore() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = reading(10L, member, book(1L), ReadingStatus.READING, true);
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading));

            bookshelfService.rateScore(member, 4.5, 10L);

            verify(readingRepository).save(readingCaptor.capture());
            assertThat(readingCaptor.getValue().getScore()).isEqualTo(4.5);
        }
    }

    @Nested
    @DisplayName("readStart")
    class ReadStart {

        @Test
        @DisplayName("독서를 시작 처리한다")
        void startsReading() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = reading(10L, member, book(1L), ReadingStatus.NOT_START, true);
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading));

            bookshelfService.readStart(member, 10L);

            verify(readingRepository).save(readingCaptor.capture());
            assertThat(readingCaptor.getValue().getStatus()).isEqualTo(ReadingStatus.READING);
            assertThat(readingCaptor.getValue().getStartedAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 12, 0));
        }
    }

    @Nested
    @DisplayName("readOver")
    class ReadOver {

        @Test
        @DisplayName("독서를 완료 처리한다")
        void finishesReading() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = reading(10L, member, book(1L), ReadingStatus.READING, true);
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading));

            bookshelfService.readOver(member, 10L);

            verify(readingRepository).save(readingCaptor.capture());
            assertThat(readingCaptor.getValue().getStatus()).isEqualTo(ReadingStatus.DONE);
            assertThat(readingCaptor.getValue().getEndedAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 12, 0));
        }
    }

    @Nested
    @DisplayName("deleteBook")
    class DeleteBook {

        @Test
        @DisplayName("독서를 삭제한다")
        void deletesReading() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = reading(10L, member, book(1L), ReadingStatus.READING, true);
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading));

            bookshelfService.deleteBook(member, 10L);

            verify(readingRepository).delete(reading);
        }
    }

    @Nested
    @DisplayName("modifyVisibility")
    class ModifyVisibility {

        @Test
        @DisplayName("공개 여부를 변경한다")
        void updatesVisibility() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = reading(10L, member, book(1L), ReadingStatus.READING, false);
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading));

            ReadingVisibilityUpdateResponse response = bookshelfService.modifyVisibility(member, 10L, true);

            verify(readingRepository).save(readingCaptor.capture());
            assertThat(readingCaptor.getValue().getIsPublic()).isTrue();
            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.isPublic()).isTrue();
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }

    private Book book(Long id) {
        return Book.builder()
                .id(id)
                .title("book")
                .author("author")
                .image("https://example.com/book.png")
                .isbn("isbn-1")
                .build();
    }

    private Reading reading(Long id, Member member, Book book, ReadingStatus status, boolean isPublic) {
        return Reading.builder()
                .id(id)
                .member(member)
                .book(book)
                .status(status)
                .isPublic(isPublic)
                .build();
    }
}
