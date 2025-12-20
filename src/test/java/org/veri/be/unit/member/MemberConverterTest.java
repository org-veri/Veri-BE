package org.veri.be.unit.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.converter.MemberConverter;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;

class MemberConverterTest {

    @Nested
    @DisplayName("toMemberInfoResponse")
    class ToMemberInfoResponse {

        @Test
        @DisplayName("회원 정보와 집계 값을 응답으로 변환한다")
        void mapsMemberInfo() {
            Member member = Member.builder()
                    .id(1L)
                    .email("member@test.com")
                    .nickname("member")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-1")
                    .providerType(ProviderType.KAKAO)
                    .build();

            MemberResponse.MemberInfoResponse response = MemberConverter.toMemberInfoResponse(member, 2, 3);

            assertThat(response.getEmail()).isEqualTo("member@test.com");
            assertThat(response.getNickname()).isEqualTo("member");
            assertThat(response.getImage()).isEqualTo("https://example.com/profile.png");
            assertThat(response.getNumOfReadBook()).isEqualTo(2);
            assertThat(response.getNumOfCard()).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("인스턴스를 생성할 수 있다")
    void canInstantiate() {
        assertThat(new MemberConverter()).isNotNull();
    }
}
