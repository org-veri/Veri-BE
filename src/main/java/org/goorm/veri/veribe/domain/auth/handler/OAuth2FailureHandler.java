package org.goorm.veri.veribe.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2ErrorCode;
import org.namul.api.payload.code.dto.supports.DefaultResponseErrorReasonDTO;
import org.namul.api.payload.writer.FailureResponseWriter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final FailureResponseWriter<DefaultResponseErrorReasonDTO> failureResponseWriter;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        DefaultResponseErrorReasonDTO reason = OAuth2ErrorCode.FAIL_OAUTH2_LOGIN.getReason();
        response.setStatus(reason.getHttpStatus().value());
        objectMapper.writeValue(response.getOutputStream(), failureResponseWriter.onFailure(reason, exception.getMessage()));
    }
}
