package org.goorm.veri.veribe.domain.book.exception;

import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;

public class MemberBookException extends ServerApplicationException {

    public MemberBookException(BaseErrorCode code) {
        super(code);
    }
}
