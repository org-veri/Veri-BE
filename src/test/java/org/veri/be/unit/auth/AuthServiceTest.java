package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.veri.be.domain.auth.service.AuthService;
import org.veri.be.domain.auth.service.TokenBlacklistStore;
import org.veri.be.domain.auth.service.TokenStorageService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.dto.ReissueTokenResponse;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;
import org.veri.be.global.auth.token.TokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    TokenStorageService tokenStorageService;

    @Mock
    TokenBlacklistStore tokenBlacklistStore;

    @Mock
    MemberRepository memberRepository;

    @Mock
    TokenProvider tokenProvider;

    Clock fixedClock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneId.of("UTC"));

    AuthService authService;

    @Captor
    ArgumentCaptor<Member> memberCaptor;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                memberQueryService,
                tokenStorageService,
                tokenBlacklistStore,
                memberRepository,
                tokenProvider,
                fixedClock
        );
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("액세스/리프레시 토큰을 발급하고 저장한다")
        void issuesTokensAndStoresRefresh() {
            Member member = member(1L, "member@test.com", "member");
            given(tokenProvider.generateAccessToken(any()))
                    .willReturn(new TokenProvider.TokenGeneration("access", fixedClock.millis() + 1000));
            given(tokenProvider.generateRefreshToken(1L))
                    .willReturn(new TokenProvider.TokenGeneration("refresh", fixedClock.millis() + 2000));

            LoginResponse response = authService.login(member);

            assertThat(response.getAccessToken()).isEqualTo("access");
            assertThat(response.getRefreshToken()).isEqualTo("refresh");
            verify(tokenStorageService).addRefreshToken(eq(1L), eq("refresh"), eq(fixedClock.millis() + 2000));
        }
    }

    @Nested
    @DisplayName("reissueToken")
    class ReissueToken {

        @Test
        @DisplayName("리프레시 토큰으로 액세스 토큰을 재발급한다")
        void reissuesAccessToken() {
            Claims claims = Jwts.claims()
                    .add("id", 1L)
                    .build();
            given(tokenProvider.parseRefreshToken("refresh")).willReturn(claims);
            Member member = member(1L, "member@test.com", "member");
            given(memberQueryService.findById(1L)).willReturn(member);
            given(tokenProvider.generateAccessToken(any()))
                    .willReturn(new TokenProvider.TokenGeneration("new-access", fixedClock.millis() + 1000));

            ReissueTokenRequest request = new ReissueTokenRequest();
            ReflectionTestUtils.setField(request, "refreshToken", "refresh");
            ReissueTokenResponse response = authService.reissueToken(request);

            assertThat(response.getAccessToken()).isEqualTo("new-access");
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("남은 시간만큼 블랙리스트에 등록한다")
        void blacklistsAccessAndRefresh() {
            Claims accessClaims = Jwts.claims()
                    .add("id", 1L)
                    .setExpiration(Date.from(Instant.parse("2030-01-01T00:01:00Z")))
                    .build();
            Claims refreshClaims = Jwts.claims()
                    .setExpiration(Date.from(Instant.parse("2030-01-01T00:02:00Z")))
                    .build();

            given(tokenProvider.parseAccessToken("access")).willReturn(accessClaims);
            given(tokenProvider.parseRefreshToken("refresh")).willReturn(refreshClaims);
            given(tokenStorageService.getRefreshToken(1L)).willReturn("refresh");

            authService.logout("access");

            verify(tokenBlacklistStore).addBlackList(eq("access"), eq(60_000L));
            verify(tokenBlacklistStore).addBlackList(eq("refresh"), eq(120_000L));
            verify(tokenStorageService).deleteRefreshToken(1L);
        }

        @Test
        @DisplayName("리프레시 토큰이 없으면 리프레시 블랙리스트는 등록하지 않는다")
        void skipsRefreshWhenMissing() {
            Claims accessClaims = Jwts.claims()
                    .add("id", 1L)
                    .setExpiration(Date.from(Instant.parse("2030-01-01T00:01:00Z")))
                    .build();
            given(tokenProvider.parseAccessToken("access")).willReturn(accessClaims);
            given(tokenStorageService.getRefreshToken(1L)).willReturn(null);

            authService.logout("access");

            verify(tokenBlacklistStore).addBlackList(eq("access"), eq(60_000L));
            verify(tokenBlacklistStore, never()).addBlackList(eq("refresh"), any(Long.class));
        }
    }

    @Nested
    @DisplayName("loginWithOAuth2")
    class LoginWithOAuth2 {

        @Test
        @DisplayName("기존 회원이면 그대로 로그인한다")
        void logsInExistingMember() {
            Member member = member(1L, "member@test.com", "member");
            OAuth2UserInfo info = OAuth2UserInfo.builder()
                    .email("member@test.com")
                    .nickname("member")
                    .image("https://example.com/profile.png")
                    .providerId("provider-1")
                    .providerType(ProviderType.KAKAO)
                    .build();
            given(memberRepository.findByProviderIdAndProviderType("provider-1", ProviderType.KAKAO))
                    .willReturn(Optional.of(member));
            given(tokenProvider.generateAccessToken(any()))
                    .willReturn(new TokenProvider.TokenGeneration("access", fixedClock.millis() + 1000));
            given(tokenProvider.generateRefreshToken(1L))
                    .willReturn(new TokenProvider.TokenGeneration("refresh", fixedClock.millis() + 2000));

            LoginResponse response = authService.loginWithOAuth2(info);

            assertThat(response.getAccessToken()).isEqualTo("access");
        }

        @Test
        @DisplayName("닉네임 충돌이면 suffix를 붙여 저장한다")
        void updatesNicknameWhenDuplicate() {
            OAuth2UserInfo info = OAuth2UserInfo.builder()
                    .email("new@test.com")
                    .nickname("dup")
                    .image("https://example.com/profile.png")
                    .providerId("provider-2")
                    .providerType(ProviderType.KAKAO)
                    .build();
            given(memberRepository.findByProviderIdAndProviderType("provider-2", ProviderType.KAKAO))
                    .willReturn(Optional.empty());
            given(memberQueryService.existsByNickname("dup")).willReturn(true);
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
                Member saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 2L);
                return saved;
            });
            given(tokenProvider.generateAccessToken(any()))
                    .willReturn(new TokenProvider.TokenGeneration("access", fixedClock.millis() + 1000));
            given(tokenProvider.generateRefreshToken(2L))
                    .willReturn(new TokenProvider.TokenGeneration("refresh", fixedClock.millis() + 2000));

            authService.loginWithOAuth2(info);

            verify(memberRepository).save(memberCaptor.capture());
            Member saved = memberCaptor.getValue();
            assertThat(saved.getNickname()).startsWith("dup_");
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + id)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
