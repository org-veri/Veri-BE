package org.veri.be.unit.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.support.assertion.ExceptionAssertions;

class MemberEntityTest {

    @Nested
    @DisplayName("updateInfo")
    class UpdateInfo {

        @Test
        @DisplayName("값이 전달되면 닉네임과 이미지를 변경한다")
        void updatesFields() {
            Member member = member("old", "https://example.com/old.png");

            member.updateInfo("new", "https://example.com/new.png");

            assertThat(member.getNickname()).isEqualTo("new");
            assertThat(member.getProfileImageUrl()).isEqualTo("https://example.com/new.png");
        }

        @Test
        @DisplayName("null 값은 기존 값을 유지한다")
        void keepsExistingValues() {
            Member member = member("old", "https://example.com/old.png");

            member.updateInfo(null, null);

            assertThat(member.getNickname()).isEqualTo("old");
            assertThat(member.getProfileImageUrl()).isEqualTo("https://example.com/old.png");
        }
    }

    @Nested
    @DisplayName("authorizeMember")
    class AuthorizeMember {

        @Test
        @DisplayName("다른 사용자면 예외가 발생한다")
        void throwsWhenDifferentMember() {
            Member member = member("member", "https://example.com/profile.png");

            ExceptionAssertions.assertApplicationException(
                    () -> member.authorizeMember(2L),
                    CommonErrorCode.DOES_NOT_HAVE_PERMISSION
            );
        }

        @Test
        @DisplayName("본인 ID면 통과한다")
        void allowsWhenSameMember() {
            Member member = member("member", "https://example.com/profile.png");

            org.assertj.core.api.Assertions.assertThatCode(() -> member.authorizeMember(1L))
                    .doesNotThrowAnyException();
        }
    }

    private Member member(String nickname, String imageUrl) {
        return Member.builder()
                .id(1L)
                .email("member@test.com")
                .nickname(nickname)
                .profileImageUrl(imageUrl)
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
