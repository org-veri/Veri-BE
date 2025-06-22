package org.goorm.veri.veribe.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.auth.service.AuthService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그인 API")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/oauth2/{provider}")
    @Operation(summary = "소셜 로그인 API", description = "Provider와 인가코드를 이용하여 로그인")
    public DefaultResponse<OAuth2Response.OAuth2LoginResponse> login(@PathVariable String provider, @RequestParam("code") String code) {
        OAuth2Response.OAuth2LoginResponse response = authService.login(provider, code);
        return DefaultResponse.ok(response);
    }
}
