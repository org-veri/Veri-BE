package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MemberContext {

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

    public static void clear() {
        currentMemberId.remove();
        currentToken.remove();

        log.debug("Member cleared");
    }
}
