package org.veri.be.global.auth.context;

import lombok.extern.slf4j.Slf4j;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.lib.exception.http.UnAuthorizedException;

import java.util.Optional;

@Slf4j
public class MemberContext {

    public static final ScopedValue<Member> member = ScopedValue.newInstance();
    public static final ScopedValue<String> token = ScopedValue.newInstance();

    public static void setToken(String token) {
        ScopedValue.where(MemberContext.token, token).run(() -> {
        });
    }

    public static void setMember(Member member) {
        ScopedValue.where(MemberContext.member, member).run(() -> {
            log.info("Member set to {}", member);
        });
    }

    public static Optional<Member> getMember() {
        if (!member.isBound()) return Optional.empty();
        return Optional.ofNullable(member.get());
    }

    public static Member getMemberOrThrow() {
        return getMember().orElseThrow(
                () -> new UnAuthorizedException(AuthErrorInfo.UNAUTHORIZED)
        );
    }
}
