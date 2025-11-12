package org.veri.be.domain.auth.service.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.veri.be.domain.auth.exception.AuthErrorInfo;
import org.veri.be.lib.exception.http.BadRequestException;

@Slf4j
@Component
public class CustomAuthExceptionHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) {
        log.debug(exception.getMessage(), exception);
        throw new BadRequestException(AuthErrorInfo.UNAUTHORIZED);
    }
}
