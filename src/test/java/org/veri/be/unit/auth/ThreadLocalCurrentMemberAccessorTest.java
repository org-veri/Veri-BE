package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;

class ThreadLocalCurrentMemberAccessorTest {

    @Nested
    @DisplayName("getCurrentMember")
    class GetCurrentMember {

        @Test
        @DisplayName("MemberContext 값을 그대로 반환한다")
        void returnsMemberFromContext() {
            Member member = member(1L, "member@test.com", "member");
            MemberContext.setCurrentMember(member);
            ThreadLocalCurrentMemberAccessor accessor = new ThreadLocalCurrentMemberAccessor();

            Optional<Member> result = accessor.getCurrentMember();

            assertThat(result).contains(member);
            MemberContext.clear();
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
