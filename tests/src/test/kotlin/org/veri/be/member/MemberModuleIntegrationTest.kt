package org.veri.be.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.modulith.test.ApplicationModuleTest
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.service.CardCountProvider
import org.veri.be.member.service.MemberCommandService
import org.veri.be.member.service.MemberRepository
import org.veri.be.member.service.ReadingCountProvider
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@ApplicationModuleTest
@Import(MemberModuleIntegrationTest.MemberModuleTestConfig::class)
class MemberModuleIntegrationTest @Autowired constructor(
    private val memberCommandService: MemberCommandService,
    private val memberRepository: MemberRepository
) {

    @Test
    fun `중복 닉네임이면 suffix를 추가한다`() {
        memberRepository.save(member("dup", "dup@veri.be", "dup-provider"))

        val saved = memberCommandService.saveOrGetOAuthMember(
            "member@veri.be",
            "dup",
            "https://example.com/profile.png",
            "provider-1",
            ProviderType.KAKAO
        )

        assertThat(saved.nickname).isEqualTo("dup_1234")
    }

    private fun member(nickname: String, email: String, providerId: String): Member {
        return Member.builder()
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId(providerId)
            .providerType(ProviderType.KAKAO)
            .build()
    }

    @TestConfiguration
    class MemberModuleTestConfig {

        @Bean
        fun readingCountProvider(): ReadingCountProvider = ReadingCountProvider { 0L }

        @Bean
        fun cardCountProvider(): CardCountProvider = CardCountProvider { 0L }

        @Bean
        fun fixedClock(): Clock {
            return Clock.fixed(Instant.ofEpochMilli(1234L), ZoneId.of("UTC"))
        }
    }
}
