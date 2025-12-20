package org.veri.be.unit.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("멤버 정보 응답으로 변환한다")
        void convertsToResponse() {
            Member member = member();

            MemberResponse.MemberInfoResponse response = MemberConverter.toMemberInfoResponse(member, 2, 3);

            assertThat(response.getEmail()).isEqualTo(member.getEmail());
            assertThat(response.getNickname()).isEqualTo(member.getNickname());
            assertThat(response.getImage()).isEqualTo(member.getProfileImageUrl());
            assertThat(response.getNumOfReadBook()).isEqualTo(2);
            assertThat(response.getNumOfCard()).isEqualTo(3);
        }
    }

    private Member member() {
        return Member.builder()
                .id(1L)
                .email("member@test.com")
                .nickname("member")
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build();
    }

    @Test
    @DisplayName("인스턴스화를 방지한다")
    void canNotInstantiate() {
        assertThatThrownBy(() -> {
            java.lang.reflect.Constructor<MemberConverter> constructor = MemberConverter.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }
}
