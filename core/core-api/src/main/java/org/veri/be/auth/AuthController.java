package org.veri.be.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.veri.be.auth.service.AuthService;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.dto.ReissueTokenResponse;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/auth/reissue")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 이용하여 액세스 토큰을 재발급합니다.")
    public ApiResponse<ReissueTokenResponse> reissueToken(@RequestBody @Valid ReissueTokenRequest request) {
        ReissueTokenResponse response = authService.reissueToken(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/api/v1/auth/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다.")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        if (token == null) {
            throw ApplicationException.of(CommonErrorCode.INVALID_REQUEST);
        }
        authService.logout(token);
        return ApiResponse.noContent();
    }
}
