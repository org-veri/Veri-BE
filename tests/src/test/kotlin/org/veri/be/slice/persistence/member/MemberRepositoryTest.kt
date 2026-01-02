package org.veri.be.slice.persistence.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport
import org.veri.be.support.fixture.MemberFixture

class MemberRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Nested
    @DisplayName("findByProviderIdAndProviderType")
    inner class FindByProviderIdAndProviderType {

        @Test
        @DisplayName("provider 정보로 조회하면 → 회원을 반환한다")
        fun returnsMemberByProvider() {
            val member = memberRepository.save(
                MemberFixture.aMember()
                    .email("member@test.com")
                    .nickname("tester")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-id")
                    .providerType(ProviderType.KAKAO)
                    .build()
            )

            val found = memberRepository.findByProviderIdAndProviderType(
                "provider-id",
                ProviderType.KAKAO
            )

            assertThat(found).isPresent
            assertThat(found.get().id).isEqualTo(member.id)
        }
    }

    @Nested
    @DisplayName("existsByNickname")
    inner class ExistsByNickname {

        @Test
        @DisplayName("닉네임이 존재하면 → true를 반환한다")
        fun returnsTrueWhenNicknameExists() {
            memberRepository.save(
                MemberFixture.aMember()
                    .email("member@test.com")
                    .nickname("tester")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-id")
                    .providerType(ProviderType.KAKAO)
                    .build()
            )

            val exists = memberRepository.existsByNickname("tester")

            assertThat(exists).isTrue()
        }

        @Test
        @DisplayName("닉네임이 존재하지 않으면 → false를 반환한다")
        fun returnsFalseWhenNicknameMissing() {
            val exists = memberRepository.existsByNickname("missing")

            assertThat(exists).isFalse()
        }
    }
}
