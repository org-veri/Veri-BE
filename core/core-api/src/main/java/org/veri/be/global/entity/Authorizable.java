package org.veri.be.global.entity;

import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

public interface Authorizable {

    boolean authorizeMember(Long memberId);

    default void authorizeOrThrow(Long memberId) throws ApplicationException {
        if (!authorizeMember(memberId)) {
            throw ApplicationException.of(CommonErrorCode.DOES_NOT_HAVE_PERMISSION);
        }
    }
}
