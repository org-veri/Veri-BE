package org.veri.be.global.auth.guards;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.global.auth.context.CurrentMemberInfo;
import org.veri.be.lib.auth.guard.Guard;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberGuard implements Guard {

    private final org.veri.be.global.auth.context.CurrentMemberAccessor currentMemberAccessor;

    @Override
    public void canActivate() {
        CurrentMemberInfo member = currentMemberAccessor.getCurrentMemberInfoOrNull();
        if (!checkMemberHasRole(member)) {
            throw ApplicationException.of(CommonErrorCode.DOES_NOT_HAVE_PERMISSION);
        }
    }

    private boolean checkMemberHasRole(CurrentMemberInfo member) {
        return member != null;
    }
}
