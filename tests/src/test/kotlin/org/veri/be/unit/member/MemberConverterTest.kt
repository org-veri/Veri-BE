package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.domain.member.dto.MemberResponse
import org.veri.be.support.fixture.MemberFixture

class MemberConverterTest {

    @Nested
    @DisplayName("toMemberInfoResponse")
    inner class ToMemberInfoResponse {

        @Test
        @DisplayName("멤버 정보 응답으로 변환하면 → 결과를 반환한다")
        fun convertsToResponse() {
            val member = member()

            val response: MemberResponse.MemberInfoResponse =
                MemberResponse.MemberInfoResponse.from(member, 2, 3)

            assertThat(response.email).isEqualTo(member.email)
            assertThat(response.nickname).isEqualTo(member.nickname)
            assertThat(response.image).isEqualTo(member.profileImageUrl)
            assertThat(response.numOfReadBook).isEqualTo(2)
            assertThat(response.numOfCard).isEqualTo(3)
        }
    }

    private fun member(): org.veri.be.domain.member.entity.Member {
        return MemberFixture.aMember().id(1L).nickname("member").build()
    }

}
