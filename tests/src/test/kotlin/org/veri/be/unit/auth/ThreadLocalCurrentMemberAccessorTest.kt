package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.lib.auth.context.MemberContext
import org.veri.be.member.auth.context.MemberRequestContext
import org.veri.be.member.auth.context.ThreadLocalCurrentMemberAccessor
import org.veri.be.member.service.MemberQueryService
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ThreadLocalCurrentMemberAccessorTest {

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    @org.mockito.InjectMocks
    private lateinit var accessor: ThreadLocalCurrentMemberAccessor

    @AfterEach
    fun tearDown() {
        MemberContext.clear()
        MemberRequestContext.clear()
    }

    @Nested
    @DisplayName("getCurrentMember")
    inner class GetCurrentMember {

        @Test
        @DisplayName("Context에 Member가 있으면 그대로 반환한다 (Cache)")
        fun returnsMemberFromContext() {
            val member = member(1L, "member@test.com", "member")
            MemberRequestContext.setCurrentMember(member)

            val result: Optional<Member> = accessor.currentMember

            assertThat(result).contains(member)
        }

        @Test
        @DisplayName("Context에 Member가 없고 ID만 있으면 DB에서 조회한다 (Lazy Load)")
        fun loadsMemberFromDbWhenIdPresent() {
            val member = member(1L, "member@test.com", "member")
            MemberContext.setCurrentMemberId(1L)
            given(memberQueryService.findOptionalById(1L)).willReturn(Optional.of(member))

            val result: Optional<Member> = accessor.currentMember

            assertThat(result).contains(member)
            verify(memberQueryService).findOptionalById(1L)

            assertThat(MemberRequestContext.getCurrentMember()).contains(member)
        }

        @Test
        @DisplayName("Context에 아무것도 없으면 Empty를 반환한다")
        fun returnsEmptyWhenContextEmpty() {
            val result: Optional<Member> = accessor.currentMember

            assertThat(result).isEmpty()
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
