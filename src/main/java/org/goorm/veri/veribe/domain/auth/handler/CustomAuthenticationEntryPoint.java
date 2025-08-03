package org.goorm.veri.veribe.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.exception.AuthErrorInfo;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException
    ) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> apiResponse = ApiResponse.error(AuthErrorInfo.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

}
