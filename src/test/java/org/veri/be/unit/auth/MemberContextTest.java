package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.support.assertion.ExceptionAssertions;

class MemberContextTest {

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Nested
    @DisplayName("getCurrentMember")
    class GetCurrentMember {

        @Test
        @DisplayName("저장된 멤버를 반환한다")
        void returnsStoredMember() {
            Member member = member();
            MemberContext.setCurrentMember(member);

            assertThat(MemberContext.getCurrentMember()).contains(member);
        }
    }

    @Nested
    @DisplayName("getMemberOrThrow")
    class GetMemberOrThrow {

        @Test
        @DisplayName("멤버가 없으면 예외가 발생한다")
        void throwsWhenMissing() {
            MemberContext.clear();

            ExceptionAssertions.assertApplicationException(
                    MemberContext::getMemberOrThrow,
                    AuthErrorInfo.UNAUTHORIZED
            );
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        @DisplayName("멤버와 토큰을 비운다")
        void clearsContext() {
            MemberContext.setCurrentMember(member());
            MemberContext.setCurrentToken("token");

            MemberContext.clear();

            assertThat(MemberContext.getCurrentMember()).isEmpty();
            assertThat(MemberContext.currentToken.get()).isNull();
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
    @DisplayName("인스턴스를 생성할 수 있다")
    void canInstantiate() {
        assertThat(new MemberContext()).isNotNull();
    }
}
