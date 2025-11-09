package org.veri.be.api.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.veri.be.domain.auth.dto.AuthRequest;
import org.veri.be.domain.auth.dto.AuthResponse;
import org.veri.be.domain.auth.service.AuthService;
import org.veri.be.lib.auth.util.UrlUtil;
import org.veri.be.lib.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증")
public class AuthController {

    private final AuthService authService;

//    @GetMapping("/oauth2/authorize/{provider}")


    @GetMapping("/api/v1/oauth2/{provider}")
    @Operation(summary = "소셜 로그인 콜백", description = "Provider와 인가코드를 이용하여 로그인")
    public ApiResponse<AuthResponse.LoginResponse> login(
            @PathVariable String provider,
            @RequestParam("code") String code,
            HttpServletRequest request
    ) {
        String origin = UrlUtil.getRequestingUrl(request);
        AuthResponse.LoginResponse response = authService.login(provider, code, origin);
        return ApiResponse.ok(response);
    }

    @PostMapping("/api/v1/auth/reissue")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 이용하여 액세스 토큰을 재발급합니다.")
    public ApiResponse<AuthResponse.ReissueTokenResponse> reissueToken(@RequestBody AuthRequest.AuthReissueRequest request) {
        AuthResponse.ReissueTokenResponse response = authService.reissueToken(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/api/v1/auth/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다.")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        authService.logout(token);
        return ApiResponse.noContent();
    }
}
