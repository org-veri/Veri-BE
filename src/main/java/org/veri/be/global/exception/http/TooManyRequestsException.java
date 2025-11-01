package org.veri.be.global.exception.http;

import org.veri.be.global.exception.ApplicationException;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends ApplicationException {

  public TooManyRequestsException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.TOO_MANY_REQUESTS);
  }
}
