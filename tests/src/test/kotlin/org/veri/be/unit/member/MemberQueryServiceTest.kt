package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.card.repository.CardRepository
import org.veri.be.domain.member.dto.MemberResponse
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.exception.MemberErrorCode
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.member.service.MemberQueryService
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor
import org.veri.be.support.assertion.ExceptionAssertions
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberQueryServiceTest {

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    @org.mockito.Mock
    private lateinit var readingRepository: ReadingRepository

    @org.mockito.Mock
    private lateinit var cardRepository: CardRepository

    @org.mockito.Mock
    private lateinit var threadLocalCurrentMemberAccessor: ThreadLocalCurrentMemberAccessor

    private lateinit var memberQueryService: MemberQueryService

    @BeforeEach
    fun setUp() {
        memberQueryService = MemberQueryService(
            memberRepository,
            readingRepository,
            cardRepository,
            threadLocalCurrentMemberAccessor
        )
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

            given(readingRepository.countAllByMemberId(1L)).willReturn(3)
            given(cardRepository.countAllByMemberId(1L)).willReturn(2)
            given(threadLocalCurrentMemberAccessor.memberOrThrow).willReturn(member)

            val response: MemberResponse.MemberInfoResponse =
                memberQueryService.findMyInfo(CurrentMemberInfo.from(JwtClaimsPayload(member.id, member.email, member.nickname, false)))

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
