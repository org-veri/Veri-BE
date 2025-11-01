package org.veri.be.global.auth.guards;

import lombok.RequiredArgsConstructor;
import org.veri.be.global.auth.context.AccessTokenMember;
import org.veri.be.global.auth.context.AccessTokenMemberContext;
import org.veri.be.global.exception.CommonErrorInfo;
import org.veri.be.global.exception.http.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberGuard implements Guard {

    @Override
    public void canActivate() throws Exception {
        Optional<AccessTokenMember> member = AccessTokenMemberContext.getAccessTokenMember();
        if (!checkMemberHasRole(member)) {
            throw new ForbiddenException(CommonErrorInfo.DOES_NOT_HAVE_PERMISSION);
        }
    }

    private boolean checkMemberHasRole(Optional<AccessTokenMember> member) {
        return member.isPresent();
    }
}
