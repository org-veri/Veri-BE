package org.veri.be.global.auth.context;

import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.http.UnAuthorizedException;

import java.util.Optional;

public interface CurrentMemberAccessor {

    Optional<Member> getCurrentMember();

    default Member getMemberOrThrow() {
        return getCurrentMember().orElseThrow(() -> new UnAuthorizedException(AuthErrorInfo.UNAUTHORIZED));
    }
}
