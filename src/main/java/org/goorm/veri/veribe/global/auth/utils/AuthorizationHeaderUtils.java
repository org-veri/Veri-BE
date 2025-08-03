package org.goorm.veri.veribe.global.auth.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class AuthorizationHeaderUtils {

  public static final String BEARER = "Bearer ";

  public Optional<String> extractTokenFromAuthorizationHeader(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(AUTHORIZATION);
    if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
      return Optional.empty();
    }
    return Optional.of(authorizationHeader.substring(BEARER.length()).trim());
  }
}
