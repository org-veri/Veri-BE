package org.veri.be.member.auth.context;

import org.veri.be.member.entity.Member;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Optional;

public interface CurrentMemberAccessor {

    Optional<Member> getCurrentMember();

    default Member getMemberOrThrow() {
        return getCurrentMember().orElseThrow(() -> ApplicationException.of(AuthErrorInfo.UNAUTHORIZED));
    }
}
