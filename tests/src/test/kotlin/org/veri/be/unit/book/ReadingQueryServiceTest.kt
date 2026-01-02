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
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.veri.be.domain.book.controller.enums.ReadingSortType
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.book.entity.enums.ReadingStatus
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.book.repository.dto.BookPopularQueryResult
import org.veri.be.domain.book.service.ReadingQueryService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.JwtClaimsPayload
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
class ReadingQueryServiceTest {

    @org.mockito.Mock
    private lateinit var readingRepository: ReadingRepository

    @org.mockito.Mock
    private lateinit var currentMemberAccessor: CurrentMemberAccessor

    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-01-03T12:00:00Z"), ZoneId.of("UTC"))

    private lateinit var readingQueryService: ReadingQueryService

    @org.mockito.Captor
    private lateinit var pageableCaptor: ArgumentCaptor<Pageable>

    @org.mockito.Captor
    private lateinit var dateTimeCaptor: ArgumentCaptor<LocalDateTime>

    @BeforeEach
    fun setUp() {
        readingQueryService = ReadingQueryService(
            readingRepository,
            currentMemberAccessor,
            fixedClock
        )
    }

    @Nested
    @DisplayName("searchAllReadingOfMember")
    inner class SearchAllReadingOfMember {

        @Test
        @DisplayName("정렬 조건에 맞는 페이징 요청을 전달한다")
        fun passesPagingWithSort() {
            given(readingRepository.findReadingPage(any(Long::class.java), anyList(), any(Pageable::class.java)))
                .willReturn(Page.empty())

            readingQueryService.searchAllReadingOfMember(1L, listOf(ReadingStatus.READING), 1, 20, ReadingSortType.SCORE)

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
            given(currentMemberAccessor.getMemberInfoOrThrow()).willReturn(
                CurrentMemberInfo.from(JwtClaimsPayload(viewer.id, viewer.email, viewer.nickname, false))
            )

            ExceptionAssertions.assertApplicationException(
                { readingQueryService.searchDetail(10L) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }

        @Test
        @DisplayName("공개 독서는 상세 응답을 반환한다")
        fun returnsDetailForPublic() {
            val owner = member(1L, "owner@test.com", "owner")
            val reading = reading(10L, owner, book(1L), ReadingStatus.READING, true)
            given(readingRepository.findByIdWithCardsAndBook(10L)).willReturn(Optional.of(reading))
            given(currentMemberAccessor.getMemberInfoOrThrow()).willReturn(
                CurrentMemberInfo.from(JwtClaimsPayload(owner.id, owner.email, owner.nickname, false))
            )

            val result = readingQueryService.searchDetail(10L)

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

            val result = readingQueryService.searchWeeklyPopular(0, 10)

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

            val result = readingQueryService.searchMyReadingDoneCount(1L)

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

            val result: Long? = readingQueryService.searchByTitleAndAuthor(1L, "title", "author")

            assertThat(result).isNull()
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
