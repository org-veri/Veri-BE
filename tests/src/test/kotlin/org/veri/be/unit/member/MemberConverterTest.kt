package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.member.converter.MemberConverter
import org.veri.be.member.dto.MemberResponse
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import java.lang.reflect.Modifier

class MemberConverterTest {

    @Nested
    @DisplayName("toMemberInfoResponse")
    inner class ToMemberInfoResponse {

        @Test
        @DisplayName("멤버 정보 응답으로 변환한다")
        fun convertsToResponse() {
            val member = member()

            val response: MemberResponse.MemberInfoResponse = MemberConverter.toMemberInfoResponse(member, 2, 3)

            assertThat(response.email).isEqualTo(member.email)
            assertThat(response.nickname).isEqualTo(member.nickname)
            assertThat(response.image).isEqualTo(member.profileImageUrl)
            assertThat(response.numOfReadBook).isEqualTo(2)
            assertThat(response.numOfCard).isEqualTo(3)
        }
    }

    private fun member(): Member {
        return Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()
    }

    @Test
    @DisplayName("생성자는 외부에서 호출할 수 없도록 private이어야 한다")
    fun constructorShouldBePrivate() {
        val constructor = MemberConverter::class.java.getDeclaredConstructor()

        assertThat(Modifier.isPrivate(constructor.modifiers)).isTrue()
    }
}
