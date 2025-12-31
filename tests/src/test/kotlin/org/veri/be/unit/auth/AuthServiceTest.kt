package org.veri.be.unit.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import org.veri.be.domain.auth.service.AuthService
import org.veri.be.domain.auth.service.TokenBlacklistStore
import org.veri.be.domain.auth.service.TokenStorageService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.member.service.MemberQueryService
import org.veri.be.global.auth.AuthErrorInfo
import org.veri.be.global.auth.dto.LoginResponse
import org.veri.be.global.auth.dto.ReissueTokenRequest
import org.veri.be.global.auth.dto.ReissueTokenResponse
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo
import org.veri.be.global.auth.token.TokenProvider
import org.veri.be.lib.exception.ApplicationException
import org.veri.be.lib.exception.CommonErrorCode
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    @org.mockito.Mock
    private lateinit var tokenStorageService: TokenStorageService

    @org.mockito.Mock
    private lateinit var tokenBlacklistStore: TokenBlacklistStore

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    @org.mockito.Mock
    private lateinit var tokenProvider: TokenProvider

    private val fixedClock: Clock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneId.of("UTC"))

    private lateinit var authService: AuthService

    @org.mockito.Captor
    private lateinit var memberCaptor: ArgumentCaptor<Member>

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            memberQueryService,
            tokenStorageService,
            tokenBlacklistStore,
            memberRepository,
            tokenProvider,
            fixedClock
        )
    }

    @Nested
    @DisplayName("login")
    inner class Login {

        @Test
        @DisplayName("액세스/리프레시 토큰을 발급하고 저장한다")
        fun issuesTokensAndStoresRefresh() {
            val member = member(1L, "member@test.com", "member")
            given(tokenProvider.generateAccessToken(any<JwtClaimsPayload>()))
                .willReturn(TokenProvider.TokenGeneration("access", fixedClock.millis() + 1000))
            given(tokenProvider.generateRefreshToken(1L))
                .willReturn(TokenProvider.TokenGeneration("refresh", fixedClock.millis() + 2000))

            val response: LoginResponse = authService.login(member)

            assertThat(response.accessToken).isEqualTo("access")
            assertThat(response.refreshToken).isEqualTo("refresh")
            verify(tokenStorageService).addRefreshToken(1L, "refresh", fixedClock.millis() + 2000)
        }
    }

    @Nested
    @DisplayName("reissueToken")
    inner class ReissueToken {

        @Test
        @DisplayName("리프레시 토큰으로 액세스 토큰을 재발급한다")
        fun reissuesAccessToken() {
            val claims: Claims = Jwts.claims().add("id", 1L).build()
            given(tokenProvider.parseRefreshToken("refresh")).willReturn(claims)
            given(tokenStorageService.getRefreshToken(1L)).willReturn("refresh")
            val member = member(1L, "member@test.com", "member")
            given(memberQueryService.findById(1L)).willReturn(member)
            given(tokenProvider.generateAccessToken(any<JwtClaimsPayload>()))
                .willReturn(TokenProvider.TokenGeneration("new-access", fixedClock.millis() + 1000))

            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", "refresh")
            val response: ReissueTokenResponse = authService.reissueToken(request)

            assertThat(response.accessToken).isEqualTo("new-access")
        }

        @Test
        @DisplayName("리프레시 토큰이 null이면 예외를 발생한다")
        fun throwsExceptionWhenRefreshTokenIsNull() {
            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", null)

            org.junit.jupiter.api.assertThrows<ApplicationException> {
                authService.reissueToken(request)
            }.apply {
                assertThat(errorCode).isEqualTo(CommonErrorCode.INVALID_REQUEST)
            }
        }

        @Test
        @DisplayName("리프레시 토큰이 blank이면 예외를 발생한다")
        fun throwsExceptionWhenRefreshTokenIsBlank() {
            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", "   ")

            org.junit.jupiter.api.assertThrows< ApplicationException> {
                authService.reissueToken(request)
            }.apply {
                assertThat(errorCode).isEqualTo(CommonErrorCode.INVALID_REQUEST)
            }
        }

        @Test
        @DisplayName("리프레시 토큰이 블랙리스트에 있으면 예외를 발생한다")
        fun throwsExceptionWhenTokenIsBlacklisted() {
            given(tokenBlacklistStore.isBlackList("blacklisted-refresh")).willReturn(true)

            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", "blacklisted-refresh")

            val exception = org.junit.jupiter.api.assertThrows<ApplicationException> {
                authService.reissueToken(request)
            }

            assertThat(exception.errorCode).isEqualTo(AuthErrorInfo.UNAUTHORIZED)
            verify(tokenBlacklistStore).isBlackList("blacklisted-refresh")
            // 블랙리스트에 있으므로 tokenProvider.parseRefreshToken과 tokenStorageService.getRefreshToken은 호출되지 않아야 함
            verify(tokenProvider, never()).parseRefreshToken(any())
            verify(tokenStorageService, never()).getRefreshToken(any())
        }

        @Test
        @DisplayName("저장된 토큰과 일치하지 않으면 예외를 발생한다")
        fun throwsExceptionWhenTokenMismatch() {
            val claims: Claims = Jwts.claims().add("id", 1L).build()
            given(tokenProvider.parseRefreshToken("different-refresh")).willReturn(claims)
            given(tokenBlacklistStore.isBlackList("different-refresh")).willReturn(false)
            given(tokenStorageService.getRefreshToken(1L)).willReturn("stored-refresh")

            val request = ReissueTokenRequest()
            ReflectionTestUtils.setField(request, "refreshToken", "different-refresh")

            org.junit.jupiter.api.assertThrows<ApplicationException> {
                authService.reissueToken(request)
            }.apply {
                assertThat(errorCode).isEqualTo(AuthErrorInfo.UNAUTHORIZED)
            }
        }
    }

    @Nested
    @DisplayName("logout")
    inner class Logout {

        @Test
        @DisplayName("남은 시간만큼 블랙리스트에 등록한다")
        fun blacklistsAccessAndRefresh() {
            val accessClaims: Claims = Jwts.claims()
                .add("id", 1L)
                .expiration(Date.from(Instant.parse("2030-01-01T00:01:00Z")))
                .build()
            val refreshClaims: Claims = Jwts.claims()
                .expiration(Date.from(Instant.parse("2030-01-01T00:02:00Z")))
                .build()

            given(tokenProvider.parseAccessToken("access")).willReturn(accessClaims)
            given(tokenProvider.parseRefreshToken("refresh")).willReturn(refreshClaims)
            given(tokenStorageService.getRefreshToken(1L)).willReturn("refresh")

            authService.logout("access")

            verify(tokenBlacklistStore).addBlackList("access", 60_000L)
            verify(tokenBlacklistStore).addBlackList("refresh", 120_000L)
            verify(tokenStorageService).deleteRefreshToken(1L)
        }

        @Test
        @DisplayName("리프레시 토큰이 없으면 리프레시 블랙리스트는 등록하지 않는다")
        fun skipsRefreshWhenMissing() {
            val accessClaims: Claims = Jwts.claims()
                .add("id", 1L)
                .expiration(Date.from(Instant.parse("2030-01-01T00:01:00Z")))
                .build()
            given(tokenProvider.parseAccessToken("access")).willReturn(accessClaims)
            given(tokenStorageService.getRefreshToken(1L)).willReturn(null)

            authService.logout("access")

            verify(tokenBlacklistStore).addBlackList("access", 60_000L)
            verify(tokenBlacklistStore, never()).addBlackList(eq("refresh"), any(Long::class.java))
        }
    }

    @Nested
    @DisplayName("loginWithOAuth2")
    inner class LoginWithOAuth2 {

        @Test
        @DisplayName("기존 회원이면 그대로 로그인한다")
        fun logsInExistingMember() {
            val member = member(1L, "member@test.com", "member")
            val info = OAuth2UserInfo.builder()
                .email("member@test.com")
                .nickname("member")
                .image("https://example.com/profile.png")
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build()
            given(memberRepository.findByProviderIdAndProviderType("provider-1", ProviderType.KAKAO))
                .willReturn(Optional.of(member))
            given(tokenProvider.generateAccessToken(any<JwtClaimsPayload>()))
                .willReturn(TokenProvider.TokenGeneration("access", fixedClock.millis() + 1000))
            given(tokenProvider.generateRefreshToken(1L))
                .willReturn(TokenProvider.TokenGeneration("refresh", fixedClock.millis() + 2000))

            val response: LoginResponse = authService.loginWithOAuth2(info)

            assertThat(response.accessToken).isEqualTo("access")
        }

        @Test
        @DisplayName("신규 회원이면 닉네임 그대로 저장한다")
        fun savesNewMemberWithOriginalNickname() {
            val info = OAuth2UserInfo.builder()
                .email("new@test.com")
                .nickname("newbie")
                .image("https://example.com/profile.png")
                .providerId("provider-2")
                .providerType(ProviderType.KAKAO)
                .build()
            given(memberRepository.findByProviderIdAndProviderType("provider-2", ProviderType.KAKAO))
                .willReturn(Optional.empty())
            given(memberQueryService.existsByNickname("newbie")).willReturn(false)
            given(memberRepository.save(any(Member::class.java))).willAnswer { invocation ->
                val saved = invocation.getArgument<Member>(0)
                ReflectionTestUtils.setField(saved, "id", 2L)
                saved
            }
            given(tokenProvider.generateAccessToken(any<JwtClaimsPayload>()))
                .willReturn(TokenProvider.TokenGeneration("access", fixedClock.millis() + 1000))
            given(tokenProvider.generateRefreshToken(2L))
                .willReturn(TokenProvider.TokenGeneration("refresh", fixedClock.millis() + 2000))

            authService.loginWithOAuth2(info)

            verify(memberRepository).save(memberCaptor.capture())
            val saved = memberCaptor.value
            assertThat(saved.nickname).isEqualTo("newbie")
        }

        @Test
        @DisplayName("닉네임 충돌이면 suffix를 붙여 저장한다")
        fun updatesNicknameWhenDuplicate() {
            val info = OAuth2UserInfo.builder()
                .email("new@test.com")
                .nickname("dup")
                .image("https://example.com/profile.png")
                .providerId("provider-2")
                .providerType(ProviderType.KAKAO)
                .build()
            given(memberRepository.findByProviderIdAndProviderType("provider-2", ProviderType.KAKAO))
                .willReturn(Optional.empty())
            given(memberQueryService.existsByNickname("dup")).willReturn(true)
            given(memberRepository.save(any(Member::class.java))).willAnswer { invocation ->
                val saved = invocation.getArgument<Member>(0)
                ReflectionTestUtils.setField(saved, "id", 2L)
                saved
            }
            given(tokenProvider.generateAccessToken(any<JwtClaimsPayload>()))
                .willReturn(TokenProvider.TokenGeneration("access", fixedClock.millis() + 1000))
            given(tokenProvider.generateRefreshToken(2L))
                .willReturn(TokenProvider.TokenGeneration("refresh", fixedClock.millis() + 2000))

            authService.loginWithOAuth2(info)

            verify(memberRepository).save(memberCaptor.capture())
            val saved = memberCaptor.value
            assertThat(saved.nickname).startsWith("dup_")
        }
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$id")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
