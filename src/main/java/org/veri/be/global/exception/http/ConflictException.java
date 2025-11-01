package org.veri.be.global.exception.http;

import org.veri.be.global.exception.ApplicationException;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class ConflictException extends ApplicationException {

  public ConflictException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.CONFLICT);
  }
}
