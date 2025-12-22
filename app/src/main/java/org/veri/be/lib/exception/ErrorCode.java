package org.veri.be.lib.exception;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

import java.net.URI;

public interface ErrorCode {

    HttpStatus getStatus();

    String getCode();

    String getMessage();

    default String getTitle() {
        return this instanceof Enum<?> enumConstant ? enumConstant.getDeclaringClass().getSimpleName()
                : getClass().getSimpleName();
    }

    default URI getType() {
        return null;
    }

    default LogLevel getLogLevel() {
        return LogLevel.ERROR;
    }
}
