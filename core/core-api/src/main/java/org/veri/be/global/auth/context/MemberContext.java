package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.global.auth.JwtClaimsPayload;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MemberContext {

    public static final ThreadLocal<CurrentMemberInfo> currentMemberInfo = new ThreadLocal<>();
    public static final ThreadLocal<String> currentToken = new ThreadLocal<>();

    public static void setCurrentToken(String token) {
        currentToken.set(token);
    }

    public static void setCurrentMemberInfo(JwtClaimsPayload claim) {
        CurrentMemberInfo memberInfo = CurrentMemberInfo.from(claim);
        currentMemberInfo.set(memberInfo);
    }

    public static Optional<CurrentMemberInfo> getCurrentMemberInfo() {
        return Optional.ofNullable(currentMemberInfo.get());
    }

    public static Optional<Long> getCurrentMemberId() {
        return Optional.ofNullable(currentMemberInfo.get()).map(CurrentMemberInfo::id);
    }

    public static void clear() {
        currentMemberInfo.remove();
        currentToken.remove();

        log.debug("Member cleared");
    }
}
