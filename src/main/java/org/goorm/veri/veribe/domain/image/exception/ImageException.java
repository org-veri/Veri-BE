package org.goorm.veri.veribe.domain.image.exception;

import org.namul.api.payload.error.exception.ServerApplicationException;

public class ImageException extends ServerApplicationException {
    public ImageException(ImageErrorCode code) {
        super(code);
    }
}
