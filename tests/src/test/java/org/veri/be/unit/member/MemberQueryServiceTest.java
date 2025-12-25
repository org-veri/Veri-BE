package org.veri.be.unit.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.exception.MemberErrorCode;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    ReadingRepository readingRepository;

    @Mock
    CardRepository cardRepository;

    MemberQueryService memberQueryService;

    @BeforeEach
    void setUp() {
        memberQueryService = new MemberQueryService(memberRepository, readingRepository, cardRepository);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하지 않으면 NotFoundException을 던진다")
        void throwsWhenNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> memberQueryService.findById(1L),
                    MemberErrorCode.NOT_FOUND
            );
        }

        @Test
        @DisplayName("존재하면 회원을 반환한다")
        void returnsMember() {
            Member member = member(1L, "member@test.com", "member");
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            Member result = memberQueryService.findById(1L);

            assertThat(result).isEqualTo(member);
        }
    }

    @Nested
    @DisplayName("findMyInfo")
    class FindMyInfo {

        @Test
        @DisplayName("독서/카드 수를 포함한 정보를 반환한다")
        void returnsMemberInfo() {
            Member member = member(1L, "member@test.com", "member");

            given(readingRepository.countAllByMember(member)).willReturn(3);
            given(cardRepository.countAllByMemberId(1L)).willReturn(2);

            MemberResponse.MemberInfoResponse response = memberQueryService.findMyInfo(member);

            assertThat(response.getNumOfReadBook()).isEqualTo(3);
            assertThat(response.getNumOfCard()).isEqualTo(2);
            assertThat(response.getNickname()).isEqualTo("member");
        }
    }

    @Nested
    @DisplayName("existsByNickname")
    class ExistsByNickname {

        @Test
        @DisplayName("닉네임 존재 여부를 반환한다")
        void returnsExists() {
            given(memberRepository.existsByNickname("member")).willReturn(true);

            boolean exists = memberQueryService.existsByNickname("member");

            assertThat(exists).isTrue();
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
