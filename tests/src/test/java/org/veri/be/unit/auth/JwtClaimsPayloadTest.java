package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.JwtClaimsPayload;

class JwtClaimsPayloadTest {

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("멤버 정보를 클레임으로 변환한다")
        void mapsMemberToClaims() {
            Member member = Member.builder()
                    .id(1L)
                    .email("member@test.com")
                    .nickname("member")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-1")
                    .providerType(ProviderType.KAKAO)
                    .build();

            JwtClaimsPayload payload = JwtClaimsPayload.from(member);

            assertThat(payload.id()).isEqualTo(1L);
            assertThat(payload.email()).isEqualTo("member@test.com");
            assertThat(payload.nickName()).isEqualTo("member");
            assertThat(payload.isAdmin()).isFalse();
        }
    }
}
