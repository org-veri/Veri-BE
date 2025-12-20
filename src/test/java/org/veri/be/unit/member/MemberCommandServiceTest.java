package org.veri.be.unit.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.exception.MemberErrorInfo;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.member.service.MemberCommandService;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    MemberRepository memberRepository;

    MemberCommandService memberCommandService;

    @Captor
    ArgumentCaptor<Member> memberCaptor;

    @BeforeEach
    void setUp() {
        memberCommandService = new MemberCommandService(memberQueryService, memberRepository);
    }

    @Nested
    @DisplayName("updateInfo")
    class UpdateInfo {

        @Test
        @DisplayName("닉네임이 중복이면 예외가 발생한다")
        void throwsWhenNicknameDuplicate() {
            Member member = member(1L, "member@test.com", "old");
            UpdateMemberInfoRequest request = new UpdateMemberInfoRequest("dup", "https://example.com/profile.png");

            given(memberQueryService.existsByNickname("dup")).willReturn(true);

            ExceptionAssertions.assertApplicationException(
                    () -> memberCommandService.updateInfo(request, member),
                    MemberErrorInfo.ALREADY_EXIST_NICKNAME
            );
        }

        @Test
        @DisplayName("닉네임과 프로필을 수정한다")
        void updatesNicknameAndProfile() {
            Member member = member(1L, "member@test.com", "old");
            UpdateMemberInfoRequest request = new UpdateMemberInfoRequest("new", "https://example.com/new.png");

            given(memberQueryService.existsByNickname("new")).willReturn(false);
            given(memberRepository.save(member)).willReturn(member);

            MemberResponse.MemberSimpleResponse response = memberCommandService.updateInfo(request, member);

            verify(memberRepository).save(memberCaptor.capture());
            Member saved = memberCaptor.getValue();
            assertThat(saved.getNickname()).isEqualTo("new");
            assertThat(saved.getProfileImageUrl()).isEqualTo("https://example.com/new.png");
            assertThat(response.getNickname()).isEqualTo("new");
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
