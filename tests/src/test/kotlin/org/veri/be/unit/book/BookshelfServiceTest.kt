package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.veri.be.domain.book.controller.enums.ReadingSortType
import org.veri.be.domain.book.dto.book.BookPopularResponse
import org.veri.be.domain.book.dto.reading.response.ReadingVisibilityUpdateResponse
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.book.entity.enums.ReadingStatus
import org.veri.be.domain.book.exception.BookErrorCode
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.book.repository.dto.BookPopularQueryResult
import org.veri.be.domain.book.service.BookshelfService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BookshelfServiceTest {

    @org.mockito.Mock
    private lateinit var readingRepository: ReadingRepository

    @org.mockito.Mock
    private lateinit var bookRepository: BookRepository

    @org.mockito.Mock
    private lateinit var currentMemberAccessor: CurrentMemberAccessor

    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-01-03T12:00:00Z"), ZoneId.of("UTC"))

    private lateinit var bookshelfService: BookshelfService

    @org.mockito.Captor
    private lateinit var readingCaptor: ArgumentCaptor<Reading>

    @org.mockito.Captor
    private lateinit var pageableCaptor: ArgumentCaptor<Pageable>

    @org.mockito.Captor
    private lateinit var dateTimeCaptor: ArgumentCaptor<LocalDateTime>

    @BeforeEach
    fun setUp() {
        bookshelfService = BookshelfService(
            readingRepository,
            bookRepository,
            currentMemberAccessor,
            fixedClock
        )
    }

    @Nested
    @DisplayName("addToBookshelf")
    inner class AddToBookshelf {

        @Test
        @DisplayName("도서가 없으면 예외가 발생한다")
        fun throwsWhenBookMissing() {
            val member = member(1L, "member@test.com", "member")
            given(bookRepository.findById(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { bookshelfService.addToBookshelf(member, 1L, true) },
                BookErrorCode.BAD_REQUEST
            )
        }

        @Test
        @DisplayName("이미 등록된 독서가 있으면 기존 독서를 반환한다")
        fun returnsExistingReading() {
            val member = member(1L, "member@test.com", "member")
            val book = book(1L)
            val reading = reading(10L, member, book, ReadingStatus.NOT_START, true)

            given(bookRepository.findById(1L)).willReturn(Optional.of(book))
            given(readingRepository.findByMemberAndBook(1L, 1L)).willReturn(Optional.of(reading))

            val result = bookshelfService.addToBookshelf(member, 1L, true)

            assertThat(result).isEqualTo(reading)
            verify(readingRepository, never()).save(any(Reading::class.java))
        }

        @Test
        @DisplayName("신규 독서를 저장한다")
        fun savesNewReading() {
            val member = member(1L, "member@test.com", "member")
            val book = book(1L)

            given(bookRepository.findById(1L)).willReturn(Optional.of(book))
            given(readingRepository.findByMemberAndBook(1L, 1L)).willReturn(Optional.empty())
            given(readingRepository.save(any(Reading::class.java))).willAnswer { invocation ->
                invocation.getArgument(0)
            }

            val result = bookshelfService.addToBookshelf(member, 1L, false)

            verify(readingRepository).save(readingCaptor.capture())
            val saved = readingCaptor.value
            assertThat(saved.member).isEqualTo(member)
            assertThat(saved.book).isEqualTo(book)
            assertThat(saved.status).isEqualTo(ReadingStatus.NOT_START)
            assertThat(saved.isPublic).isFalse()
            assertThat(result).isEqualTo(saved)
        }
    }

    @Nested
    @DisplayName("searchAllReadingOfMember")
    inner class SearchAllReadingOfMember {

        @Test
        @DisplayName("정렬 조건에 맞는 페이징 요청을 전달한다")
        fun passesPagingWithSort() {
            given(readingRepository.findReadingPage(any(Long::class.java), anyList(), any(Pageable::class.java)))
                .willReturn(Page.empty())

            bookshelfService.searchAllReadingOfMember(1L, listOf(ReadingStatus.READING), 1, 20, ReadingSortType.SCORE)

            verify(readingRepository).findReadingPage(any(Long::class.java), anyList(), pageableCaptor.capture())
            val pageable = pageableCaptor.value
            assertThat(pageable.pageNumber).isEqualTo(1)
            assertThat(pageable.pageSize).isEqualTo(20)
            assertThat(pageable.sort).isEqualTo(ReadingSortType.SCORE.sort)
        }
    }

    @Nested
    @DisplayName("searchDetail")
    inner class SearchDetail {

        @Test
        @DisplayName("비공개 독서는 조회 권한을 확인한다")
        fun checksAuthorizationWhenPrivate() {
            val owner = member(1L, "owner@test.com", "owner")
            val viewer = member(2L, "viewer@test.com", "viewer")
            val reading = reading(10L, owner, book(1L), ReadingStatus.READING, false)

            given(readingRepository.findByIdWithCardsAndBook(10L)).willReturn(Optional.of(reading))
            given(currentMemberAccessor.memberInfoOrThrow).willReturn(CurrentMemberInfo.from(viewer))

            ExceptionAssertions.assertApplicationException(
                { bookshelfService.searchDetail(10L) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }

        @Test
        @DisplayName("공개 독서는 상세 응답을 반환한다")
        fun returnsDetailForPublic() {
            val owner = member(1L, "owner@test.com", "owner")
            val reading = reading(10L, owner, book(1L), ReadingStatus.READING, true)
            given(readingRepository.findByIdWithCardsAndBook(10L)).willReturn(Optional.of(reading))
            given(currentMemberAccessor.currentMemberInfo)
                .willReturn(Optional.of(CurrentMemberInfo.from(owner)))

            val result = bookshelfService.searchDetail(10L)

            assertThat(result.memberBookId()).isEqualTo(10L)
        }
    }

    @Nested
    @DisplayName("searchWeeklyPopular")
    inner class SearchWeeklyPopular {

        @Test
        @DisplayName("주간 인기 도서를 조회한다")
        fun queriesWeeklyPopular() {
            given(readingRepository.findMostPopularBook(any(LocalDateTime::class.java), any(LocalDateTime::class.java), any(Pageable::class.java)))
                .willReturn(PageImpl(listOf(BookPopularQueryResult("img", "title", "author", "pub", "isbn"))))

            val result = bookshelfService.searchWeeklyPopular(0, 10)

            verify(readingRepository).findMostPopularBook(dateTimeCaptor.capture(), dateTimeCaptor.capture(), any(Pageable::class.java))
            val captured = dateTimeCaptor.allValues
            assertThat(captured[0].dayOfWeek.value).isEqualTo(1)
            assertThat(captured[1]).isAfter(captured[0])
            assertThat(result.totalElements).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("searchMyReadingDoneCount")
    inner class SearchMyReadingDoneCount {

        @Test
        @DisplayName("완독 개수를 반환한다")
        fun returnsDoneCount() {
            given(readingRepository.countByStatusAndMember(ReadingStatus.DONE, 1L)).willReturn(2)

            val result = bookshelfService.searchMyReadingDoneCount(1L)

            assertThat(result).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("searchByTitleAndAuthor")
    inner class SearchByTitleAndAuthor {

        @Test
        @DisplayName("일치하는 독서가 없으면 null을 반환한다")
        fun returnsNullWhenMissing() {
            given(readingRepository.findByAuthorAndTitle(1L, "title", "author")).willReturn(Optional.empty())

            val result: Long? = bookshelfService.searchByTitleAndAuthor(1L, "title", "author")

            assertThat(result).isNull()
        }
    }

    @Nested
    @DisplayName("modifyBook")
    inner class ModifyBook {

        @Test
        @DisplayName("진행 정보를 수정한다")
        fun updatesProgress() {
            val member = member(1L, "member@test.com", "member")
            val reading = reading(10L, member, book(1L), ReadingStatus.NOT_START, true)
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading))

            bookshelfService.modifyBook(member, 4.0, LocalDateTime.now(), LocalDateTime.now(), 10L)

            verify(readingRepository).save(readingCaptor.capture())
            assertThat(readingCaptor.value.status).isEqualTo(ReadingStatus.DONE)
        }
    }

    @Nested
    @DisplayName("rateScore")
    inner class RateScore {

        @Test
        @DisplayName("평점을 수정한다")
        fun updatesScore() {
            val member = member(1L, "member@test.com", "member")
            val reading = reading(10L, member, book(1L), ReadingStatus.READING, true)
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading))

            bookshelfService.rateScore(member, 4.5, 10L)

            verify(readingRepository).save(readingCaptor.capture())
            assertThat(readingCaptor.value.score).isEqualTo(4.5)
        }
    }

    @Nested
    @DisplayName("readStart")
    inner class ReadStart {

        @Test
        @DisplayName("독서를 시작 처리한다")
        fun startsReading() {
            val member = member(1L, "member@test.com", "member")
            val reading = reading(10L, member, book(1L), ReadingStatus.NOT_START, true)
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading))

            bookshelfService.readStart(member, 10L)

            verify(readingRepository).save(readingCaptor.capture())
            assertThat(readingCaptor.value.status).isEqualTo(ReadingStatus.READING)
            assertThat(readingCaptor.value.startedAt).isEqualTo(LocalDateTime.of(2024, 1, 3, 12, 0))
        }
    }

    @Nested
    @DisplayName("readOver")
    inner class ReadOver {

        @Test
        @DisplayName("독서를 완료 처리한다")
        fun finishesReading() {
            val member = member(1L, "member@test.com", "member")
            val reading = reading(10L, member, book(1L), ReadingStatus.READING, true)
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading))

            bookshelfService.readOver(member, 10L)

            verify(readingRepository).save(readingCaptor.capture())
            assertThat(readingCaptor.value.status).isEqualTo(ReadingStatus.DONE)
            assertThat(readingCaptor.value.endedAt).isEqualTo(LocalDateTime.of(2024, 1, 3, 12, 0))
        }
    }

    @Nested
    @DisplayName("deleteBook")
    inner class DeleteBook {

        @Test
        @DisplayName("독서를 삭제한다")
        fun deletesReading() {
            val member = member(1L, "member@test.com", "member")
            val reading = reading(10L, member, book(1L), ReadingStatus.READING, true)
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading))

            bookshelfService.deleteBook(member, 10L)

            verify(readingRepository).delete(reading)
        }
    }

    @Nested
    @DisplayName("modifyVisibility")
    inner class ModifyVisibility {

        @Test
        @DisplayName("공개 여부를 변경한다")
        fun updatesVisibility() {
            val member = member(1L, "member@test.com", "member")
            val reading = reading(10L, member, book(1L), ReadingStatus.READING, false)
            given(readingRepository.findById(10L)).willReturn(Optional.of(reading))

            val response = bookshelfService.modifyVisibility(member, 10L, true)

            verify(readingRepository).save(readingCaptor.capture())
            assertThat(readingCaptor.value.isPublic).isTrue()
            assertThat(response.id()).isEqualTo(10L)
            assertThat(response.isPublic()).isTrue()
        }
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }

    private fun book(id: Long): Book {
        return Book.builder()
            .id(id)
            .title("book")
            .author("author")
            .image("https://example.com/book.png")
            .isbn("isbn-1")
            .build()
    }

    private fun reading(id: Long, member: Member, book: Book, status: ReadingStatus, isPublic: Boolean): Reading {
        return Reading.builder()
            .id(id)
            .member(member)
            .book(book)
            .status(status)
            .isPublic(isPublic)
            .build()
    }
}
