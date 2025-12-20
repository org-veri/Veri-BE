package org.veri.be.slice.persistence.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class MemberRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    MemberRepository memberRepository;

    @Nested
    @DisplayName("findByProviderIdAndProviderType")
    class FindByProviderIdAndProviderType {

        @Test
        @DisplayName("provider 정보를 기준으로 회원을 조회한다")
        void returnsMemberByProvider() {
            Member member = memberRepository.save(Member.builder()
                    .email("member@test.com")
                    .nickname("tester")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-id")
                    .providerType(ProviderType.KAKAO)
                    .build());

            Optional<Member> found = memberRepository.findByProviderIdAndProviderType(
                    "provider-id",
                    ProviderType.KAKAO
            );

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(member.getId());
        }
    }

    @Nested
    @DisplayName("existsByNickname")
    class ExistsByNickname {

        @Test
        @DisplayName("닉네임이 존재하면 true를 반환한다")
        void returnsTrueWhenNicknameExists() {
            memberRepository.save(Member.builder()
                    .email("member@test.com")
                    .nickname("tester")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-id")
                    .providerType(ProviderType.KAKAO)
                    .build());

            boolean exists = memberRepository.existsByNickname("tester");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("닉네임이 존재하지 않으면 false를 반환한다")
        void returnsFalseWhenNicknameMissing() {
            boolean exists = memberRepository.existsByNickname("missing");

            assertThat(exists).isFalse();
        }
    }
}
