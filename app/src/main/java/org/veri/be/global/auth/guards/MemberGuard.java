package org.veri.be.global.auth.guards;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.lib.auth.guard.Guard;
import org.veri.be.lib.exception.CommonErrorInfo;
import org.veri.be.lib.exception.http.ForbiddenException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberGuard implements Guard {

    private final org.veri.be.global.auth.context.CurrentMemberAccessor currentMemberAccessor;

    @Override
    public void canActivate() {
        Optional<Member> member = currentMemberAccessor.getCurrentMember();
        if (!checkMemberHasRole(member)) {
            throw new ForbiddenException(CommonErrorInfo.DOES_NOT_HAVE_PERMISSION);
        }
    }

    private boolean checkMemberHasRole(Optional<Member> member) {
        return member.isPresent();
    }
}
