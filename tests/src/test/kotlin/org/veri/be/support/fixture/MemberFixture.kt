package org.veri.be.support.fixture

import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType

object MemberFixture {
    fun aMember(): Member.MemberBuilder<*, *> {
        return Member.builder()
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
    }
}
