package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.member.dto.MemberResponse
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.exception.MemberErrorCode
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.member.service.MemberCommandService
import org.veri.be.domain.member.service.MemberQueryService
import org.veri.be.support.assertion.MemberAssert
import org.veri.be.support.assertion.ExceptionAssertions
import org.veri.be.support.fixture.MemberFixture

@ExtendWith(MockitoExtension::class)
class MemberCommandServiceTest {

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var memberCommandService: MemberCommandService

    @org.mockito.Captor
    private lateinit var memberCaptor: ArgumentCaptor<Member>

    @BeforeEach
    fun setUp() {
        memberCommandService = MemberCommandService(memberQueryService, memberRepository)
    }

    @Nested
    @DisplayName("updateInfo")
    inner class UpdateInfo {

        @Test
        @DisplayName("닉네임이 중복이면 → 예외가 발생한다")
        fun throwsWhenNicknameDuplicate() {
            val member = MemberFixture.aMember()
                .id(1L)
                .nickname("old")
                .build()
            val request = UpdateMemberInfoRequest("dup", "https://example.com/profile.png")

            given(memberRepository.findById(1L)).willReturn(java.util.Optional.of(member))
            given(memberQueryService.existsByNickname("dup")).willReturn(true)

            ExceptionAssertions.assertApplicationException(
                { memberCommandService.updateInfo(request, member.id) },
                MemberErrorCode.ALREADY_EXIST_NICKNAME
            )
        }

        @Test
        @DisplayName("닉네임과 프로필을 수정하면 → 변경된 결과를 반환한다")
        fun updatesNicknameAndProfile() {
            val member = MemberFixture.aMember()
                .id(1L)
                .nickname("old")
                .build()
            val request = UpdateMemberInfoRequest("new", "https://example.com/new.png")

            given(memberRepository.findById(1L)).willReturn(java.util.Optional.of(member))
            given(memberQueryService.existsByNickname("new")).willReturn(false)
            given(memberRepository.save(member)).willReturn(member)

            val response: MemberResponse.MemberSimpleResponse = memberCommandService.updateInfo(request, member.id)

            then(memberRepository).should().save(memberCaptor.capture())
            MemberAssert.assertThat(memberCaptor.value)
                .hasNickname("new")
                .hasProfileImageUrl("https://example.com/new.png")
            assertThat(response.nickname).isEqualTo("new")
        }
    }
}
