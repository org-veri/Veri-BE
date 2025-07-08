package org.goorm.veri.veribe.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.dto.AuthRequest;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.auth.service.AuthService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그인 API")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/api/v1/oauth2/{provider}")
    @Operation(summary = "소셜 로그인 API", description = "Provider와 인가코드를 이용하여 로그인")
    public DefaultResponse<AuthResponse.LoginResponse> login(@PathVariable String provider, @RequestParam("code") String code) {
        AuthResponse.LoginResponse response = authService.login(provider, code);
        return DefaultResponse.ok(response);
    }

    @GetMapping("/api/v2/oauth2/{provider}")
    @Operation(summary = "소셜 로그인 API V2", description = "Provider와 인가코드를 이용하여 로그인 (오리진 사용해 redirect-url 처리)")
    public DefaultResponse<AuthResponse.LoginResponse> loginV2(@PathVariable String provider,
                                                               @RequestParam("code") String code,
                                                               HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        AuthResponse.LoginResponse response = authService.login(provider, code, origin);
        return DefaultResponse.ok(response);
    }

    @PostMapping("/api/v1/auth/reissue")
    public DefaultResponse<AuthResponse.ReissueTokenResponse> reissueToken(@RequestBody AuthRequest.AuthReissueRequest request) {
        AuthResponse.ReissueTokenResponse response = authService.reissueToken(request);
        return DefaultResponse.ok(response);
    }
}
