package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;

@ExtendWith(MockitoExtension.class)
class ThreadLocalCurrentMemberAccessorTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    ThreadLocalCurrentMemberAccessor accessor;

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Nested
    @DisplayName("getCurrentMember")
    class GetCurrentMember {

        @Test
        @DisplayName("Context에 Member가 있으면 그대로 반환한다 (Cache)")
        void returnsMemberFromContext() {
            Member member = member(1L, "member@test.com", "member");
            MemberContext.setCurrentMember(member);

            Optional<Member> result = accessor.getCurrentMember();

            assertThat(result).contains(member);
        }

        @Test
        @DisplayName("Context에 Member가 없고 ID만 있으면 DB에서 조회한다 (Lazy Load)")
        void loadsMemberFromDbWhenIdPresent() {
            Member member = member(1L, "member@test.com", "member");
            MemberContext.setCurrentMemberId(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            Optional<Member> result = accessor.getCurrentMember();

            assertThat(result).contains(member);
            verify(memberRepository).findById(1L);
            
            // Verify it cached the result
            assertThat(MemberContext.getCurrentMember()).contains(member);
        }

        @Test
        @DisplayName("Context에 아무것도 없으면 Empty를 반환한다")
        void returnsEmptyWhenContextEmpty() {
            Optional<Member> result = accessor.getCurrentMember();

            assertThat(result).isEmpty();
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
