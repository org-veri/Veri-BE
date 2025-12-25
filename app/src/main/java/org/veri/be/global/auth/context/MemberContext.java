package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MemberContext {

    public static final ThreadLocal<Member> currentMember = new ThreadLocal<>();
    public static final ThreadLocal<Long> currentMemberId = new ThreadLocal<>();
    public static final ThreadLocal<String> currentToken = new ThreadLocal<>();

    public static void setCurrentToken(String token) {
        currentToken.set(token);
    }

    public static void setCurrentMemberId(Long memberId) {
        currentMemberId.set(memberId);
    }

    public static Optional<Long> getCurrentMemberId() {
        return Optional.ofNullable(currentMemberId.get());
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
                () -> ApplicationException.of(AuthErrorInfo.UNAUTHORIZED)
        );
    }

    public static void clear() {
        currentMember.remove();
        currentMemberId.remove();
        currentToken.remove();

        log.debug("Member cleared");
    }
}
