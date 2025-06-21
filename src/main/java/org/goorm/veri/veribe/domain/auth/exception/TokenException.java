package org.goorm.veri.veribe.domain.auth.exception;

import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;

public class TokenException extends ServerApplicationException {
    public TokenException(BaseErrorCode code) {
        super(code);
    }
}
