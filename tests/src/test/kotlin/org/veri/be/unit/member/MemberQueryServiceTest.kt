package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.member.dto.MemberResponse
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.exception.MemberErrorCode
import org.veri.be.member.service.CardCountProvider
import org.veri.be.member.service.MemberRepository
import org.veri.be.member.service.MemberQueryService
import org.veri.be.member.service.ReadingCountProvider
import org.veri.be.support.assertion.ExceptionAssertions
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberQueryServiceTest {

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    @org.mockito.Mock
    private lateinit var readingCountProvider: ReadingCountProvider

    @org.mockito.Mock
    private lateinit var cardCountProvider: CardCountProvider

    private lateinit var memberQueryService: MemberQueryService

    @BeforeEach
    fun setUp() {
        memberQueryService = MemberQueryService(memberRepository, readingCountProvider, cardCountProvider)
    }

    @Nested
    @DisplayName("findById")
    inner class FindById {

        @Test
        @DisplayName("존재하지 않으면 NotFoundException을 던진다")
        fun throwsWhenNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { memberQueryService.findById(1L) },
                MemberErrorCode.NOT_FOUND
            )
        }

        @Test
        @DisplayName("존재하면 회원을 반환한다")
        fun returnsMember() {
            val member = member(1L, "member@test.com", "member")
            given(memberRepository.findById(1L)).willReturn(Optional.of(member))

            val result = memberQueryService.findById(1L)

            assertThat(result).isEqualTo(member)
        }
    }

    @Nested
    @DisplayName("findMyInfo")
    inner class FindMyInfo {

        @Test
        @DisplayName("독서/카드 수를 포함한 정보를 반환한다")
        fun returnsMemberInfo() {
            val member = member(1L, "member@test.com", "member")

            given(readingCountProvider.countReadingsByMemberId(1L)).willReturn(3)
            given(cardCountProvider.countCardsByMemberId(1L)).willReturn(2)

            val response: MemberResponse.MemberInfoResponse = memberQueryService.findMyInfo(member)

            assertThat(response.numOfReadBook).isEqualTo(3)
            assertThat(response.numOfCard).isEqualTo(2)
            assertThat(response.nickname).isEqualTo("member")
        }
    }

    @Nested
    @DisplayName("existsByNickname")
    inner class ExistsByNickname {

        @Test
        @DisplayName("닉네임 존재 여부를 반환한다")
        fun returnsExists() {
            given(memberRepository.existsByNickname("member")).willReturn(true)

            val exists = memberQueryService.existsByNickname("member")

            assertThat(exists).isTrue()
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
}
