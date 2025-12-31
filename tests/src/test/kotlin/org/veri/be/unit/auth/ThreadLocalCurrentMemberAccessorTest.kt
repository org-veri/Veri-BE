package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.global.auth.context.MemberContext
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ThreadLocalCurrentMemberAccessorTest {

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    @org.mockito.InjectMocks
    private lateinit var accessor: ThreadLocalCurrentMemberAccessor

    @BeforeEach
    fun setUp() {
        // 테스트 간 캐시 격리를 위해 새 accessor 인스턴스 사용 (@InjectMocks으로 자동 생성됨)
    }

    @AfterEach
    fun tearDown() {
        MemberContext.clear()
    }

    @Nested
    @DisplayName("getCurrentMember")
    inner class GetCurrentMember {

        @Test
        @DisplayName("Context에 Member가 있으면 그대로 반환한다 (ThreadLocal Cache)")
        fun returnsMemberFromContext() {
            val member = member(1L, "member@test.com", "member")
            MemberContext.setCurrentMember(member)

            val result: Optional<Member> = accessor.currentMember

            assertThat(result).contains(member)
        }

        @Test
        @DisplayName("Context에 Member가 없고 ID만 있으면 DB에서 조회 후 Caffeine 캐시에 저장한다")
        fun loadsMemberFromDbWhenIdPresent() {
            val member = member(1L, "member@test.com", "member")
            MemberContext.setCurrentMemberId(1L)
            given(memberRepository.findById(1L)).willReturn(Optional.of(member))

            val result: Optional<Member> = accessor.currentMember

            assertThat(result).contains(member)
            verify(memberRepository).findById(1L)
            assertThat(MemberContext.getCurrentMember()).contains(member)
        }

        @Test
        @DisplayName("동일한 ID로 두 번 조회하면 두 번째는 Caffeine 캐시를 사용한다")
        fun usesCaffeineCacheOnSecondAccess() {
            val member = member(1L, "member@test.com", "member")

            // 첫 번째 요청: DB 조회
            MemberContext.setCurrentMemberId(1L)
            given(memberRepository.findById(1L)).willReturn(Optional.of(member))

            val result1: Optional<Member> = accessor.currentMember
            assertThat(result1).contains(member)
            verify(memberRepository).findById(1L)

            // Context 초기화 (다른 요청 시뮬레이션)
            MemberContext.clear()
            MemberContext.setCurrentMemberId(1L)

            // 두 번째 요청: Caffeine 캐시 사용 (DB 조회하지 않음)
            val result2: Optional<Member> = accessor.currentMember
            assertThat(result2).contains(member)
            verify(memberRepository, times(1)).findById(1L) // 여전히 1번만 호출됨
            assertThat(MemberContext.getCurrentMember()).contains(member)
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
