package org.veri.be.global.auth.context;

import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Optional;

public interface CurrentMemberAccessor {

    default Optional<CurrentMemberInfo> getCurrentMemberInfo() {
        return Optional.ofNullable(getCurrentMemberInfoOrNull());
    }

    CurrentMemberInfo getCurrentMemberInfoOrNull();

    default CurrentMemberInfo getMemberInfoOrThrow() {
        return getCurrentMemberInfo().orElseThrow(() -> ApplicationException.of(AuthErrorInfo.UNAUTHORIZED));
    }

    Optional<Member> getCurrentMember();

    default Member getMemberOrThrow() {
        return getCurrentMember().orElseThrow(() -> ApplicationException.of(AuthErrorInfo.UNAUTHORIZED));
    }
}
