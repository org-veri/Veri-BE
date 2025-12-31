package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.member.dto.MemberResponse
import org.veri.be.member.dto.UpdateMemberInfoRequest
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.exception.MemberErrorCode
import org.veri.be.member.service.MemberRepository
import org.veri.be.member.service.MemberCommandService
import org.veri.be.member.service.MemberQueryService
import org.veri.be.support.assertion.ExceptionAssertions
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class MemberCommandServiceTest {

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var memberCommandService: MemberCommandService
    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))

    @org.mockito.Captor
    private lateinit var memberCaptor: ArgumentCaptor<Member>

    @BeforeEach
    fun setUp() {
        memberCommandService = MemberCommandService(memberQueryService, memberRepository, fixedClock)
    }

    @Nested
    @DisplayName("updateInfo")
    inner class UpdateInfo {

        @Test
        @DisplayName("닉네임이 중복이면 예외가 발생한다")
        fun throwsWhenNicknameDuplicate() {
            val member = member(1L, "member@test.com", "old")
            val request = UpdateMemberInfoRequest("dup", "https://example.com/profile.png")

            given(memberQueryService.existsByNickname("dup")).willReturn(true)

            ExceptionAssertions.assertApplicationException(
                { memberCommandService.updateInfo(request, member) },
                MemberErrorCode.ALREADY_EXIST_NICKNAME
            )
        }

        @Test
        @DisplayName("닉네임과 프로필을 수정한다")
        fun updatesNicknameAndProfile() {
            val member = member(1L, "member@test.com", "old")
            val request = UpdateMemberInfoRequest("new", "https://example.com/new.png")

            given(memberQueryService.existsByNickname("new")).willReturn(false)
            given(memberRepository.save(member)).willReturn(member)

            val response: MemberResponse.MemberSimpleResponse = memberCommandService.updateInfo(request, member)

            verify(memberRepository).save(memberCaptor.capture())
            val saved = memberCaptor.value
            assertThat(saved.nickname).isEqualTo("new")
            assertThat(saved.profileImageUrl).isEqualTo("https://example.com/new.png")
            assertThat(response.nickname).isEqualTo("new")
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
