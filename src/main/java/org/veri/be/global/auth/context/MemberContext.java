package org.veri.be.global.auth.context;

import lombok.extern.slf4j.Slf4j;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.http.UnAuthorizedException;

import java.util.Optional;

@Slf4j
public class MemberContext {

    public static final ThreadLocal<Member> currentMember = new ThreadLocal<>();
    public static final ThreadLocal<String> currentToken = new ThreadLocal<>();

    public static void setCurrentToken(String token) {
        currentToken.set(token);
    }

    public static void setCurrentMember(Member member) {
        currentMember.set(member);
        log.debug("Member set to {}", member);
    }

    public static Optional<Member> getCurrentMember() {
        return Optional.ofNullable(currentMember.get());
    }

    public static Member getMemberOrThrow() {
        return getCurrentMember().orElseThrow(
                () -> new UnAuthorizedException(AuthErrorInfo.UNAUTHORIZED)
        );
    }

    public static void clear() {
        currentMember.remove();
        currentToken.remove();

        log.debug("Member cleared");
    }
}
