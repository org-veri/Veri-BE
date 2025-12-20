package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.CurrentMemberAccessor;
import org.veri.be.global.auth.guards.MemberGuard;
import org.veri.be.lib.exception.CommonErrorInfo;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class MemberGuardTest {

    @Mock
    CurrentMemberAccessor currentMemberAccessor;

    @InjectMocks
    MemberGuard guard;

    @Nested
    @DisplayName("canActivate")
    class CanActivate {

        @Test
        @DisplayName("로그인한 사용자가 있으면 통과한다")
        void allowsWhenMemberPresent() {
            given(currentMemberAccessor.getCurrentMember()).willReturn(Optional.of(member()));

            assertThatCode(() -> guard.canActivate()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("로그인한 사용자가 없으면 예외가 발생한다")
        void throwsWhenMemberMissing() {
            given(currentMemberAccessor.getCurrentMember()).willReturn(Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    guard::canActivate,
                    CommonErrorInfo.DOES_NOT_HAVE_PERMISSION
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
