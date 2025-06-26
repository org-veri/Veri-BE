package org.goorm.veri.veribe.domain.book.exception;

import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;

public class NaverAPIException extends ServerApplicationException {

    public NaverAPIException(BaseErrorCode code) {
        super(code);
    }
}
