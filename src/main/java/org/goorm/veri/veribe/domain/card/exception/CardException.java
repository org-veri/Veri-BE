package org.goorm.veri.veribe.domain.card.exception;

import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;

public class CardException extends ServerApplicationException {

    public CardException(BaseErrorCode code) {
        super(code);
    }
}
