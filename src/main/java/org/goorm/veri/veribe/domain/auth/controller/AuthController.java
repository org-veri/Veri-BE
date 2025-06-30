package org.goorm.veri.veribe.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
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
    public DefaultResponse<OAuth2Response.LoginResponse> login(@PathVariable String provider, @RequestParam("code") String code) {
        OAuth2Response.LoginResponse response = authService.login(provider, code);
        return DefaultResponse.ok(response);
    }

    @PostMapping("/api/v1/auth/reissue")
    public DefaultResponse<OAuth2Response.ReissueTokenResponse> reissueToken(@RequestBody OAuth2Request.AuthReissueRequest request) {
        OAuth2Response.ReissueTokenResponse response = authService.reissueToken(request);
        return DefaultResponse.ok(response);
    }
}
