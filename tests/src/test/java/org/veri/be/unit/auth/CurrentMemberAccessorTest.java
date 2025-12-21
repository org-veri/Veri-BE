package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.global.auth.context.CurrentMemberAccessor;
import org.veri.be.support.assertion.ExceptionAssertions;

class CurrentMemberAccessorTest {

    @Nested
    @DisplayName("getMemberOrThrow")
    class GetMemberOrThrow {

        @Test
        @DisplayName("현재 회원이 있으면 반환한다")
        void returnsMember() {
            Member member = member();
            CurrentMemberAccessor accessor = () -> Optional.of(member);

            Member result = accessor.getMemberOrThrow();

            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("현재 회원이 없으면 예외가 발생한다")
        void throwsWhenMissing() {
            CurrentMemberAccessor accessor = Optional::empty;

            ExceptionAssertions.assertApplicationException(
                    accessor::getMemberOrThrow,
                    AuthErrorInfo.UNAUTHORIZED
            );
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
}
