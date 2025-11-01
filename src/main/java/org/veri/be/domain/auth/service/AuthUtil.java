package org.veri.be.domain.auth.service;


import org.veri.be.domain.auth.exception.AuthErrorInfo;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.exception.http.UnAuthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    private AuthUtil() {
    }

    public static Member getCurrentMember() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnAuthorizedException(AuthErrorInfo.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Member) {
            return (Member) principal;
        }

        throw new UnAuthorizedException(AuthErrorInfo.UNAUTHORIZED);
    }
}
