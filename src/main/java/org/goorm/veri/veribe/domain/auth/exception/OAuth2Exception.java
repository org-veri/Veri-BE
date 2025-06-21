package org.goorm.veri.veribe.domain.auth.exception;

import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;

public class OAuth2Exception extends ServerApplicationException {

    public OAuth2Exception(BaseErrorCode code) {
        super(code);
    }
}
