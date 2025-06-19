package org.goorm.veri.veribe.domain.image.exception;

import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.error.exception.ServerApplicationException;

public class DirectoryException extends ServerApplicationException {
    public DirectoryException(BaseErrorCode code) {
        super(code);
    }
}
