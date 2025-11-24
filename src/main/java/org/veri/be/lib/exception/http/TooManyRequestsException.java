package org.veri.be.lib.exception.http;

import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends ApplicationException {

  public TooManyRequestsException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.TOO_MANY_REQUESTS);
  }
}
