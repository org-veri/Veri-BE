package org.goorm.veri.veribe.domain.member.exception;

import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;

public class MemberException extends ServerApplicationException {

    public MemberException(BaseErrorCode code) {
        super(code);
    }
}
