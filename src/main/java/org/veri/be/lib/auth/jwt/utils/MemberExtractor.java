package org.veri.be.lib.auth.jwt.utils;


import org.veri.be.global.auth.context.AccessTokenMember;
import org.veri.be.global.auth.context.AccessTokenMemberContext;
import org.veri.be.lib.exception.http.UnAuthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.lib.auth.jwt.JwtAuthenticator;
import org.veri.be.lib.auth.jwt.JwtExtractor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.veri.be.lib.auth.jwt.TokenErrorInfo;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberExtractor {

  private final JwtAuthenticator jwtAuthenticator;
  private final JwtExtractor jwtExtractor;
  private final AuthorizationHeaderUtils authorizationHeaderUtils;

  public void extractMemberFromToken() {
    HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

    Optional<String> extractAccessTokenFromAuthorizationHeader = this.authorizationHeaderUtils.extractTokenFromAuthorizationHeader(
        httpServletRequest);
    if (extractAccessTokenFromAuthorizationHeader.isEmpty()) {
      throw new UnAuthorizedException(TokenErrorInfo.NOT_FOUND_ACCESS_TOKEN);
    }

    String accessToken = extractAccessTokenFromAuthorizationHeader.get();
    this.jwtAuthenticator.verifyAccessToken(accessToken);

    AccessTokenMember accessTokenMember = AccessTokenMember.from(
        this.jwtExtractor.parseAccessTokenPayloads(accessToken));

    if (!this.isAccessTokenMemberHasAllFields(accessTokenMember)) {
      throw new UnAuthorizedException(TokenErrorInfo.NOT_FOUND_ALL_FIELDS_IN_ACCESS_TOKEN);
    }

    AccessTokenMemberContext.setAccessTokenMember(accessTokenMember);
  }

  private boolean isAccessTokenMemberHasAllFields(AccessTokenMember accessTokenMember) {
    return accessTokenMember.id() != null && accessTokenMember.email() != null && accessTokenMember.isAdmin() != null
        && accessTokenMember.nickName() != null;
  }
}
