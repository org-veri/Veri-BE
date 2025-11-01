package org.veri.be.global.exception.http;

import org.veri.be.global.exception.ApplicationException;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class RequestTimeoutException extends ApplicationException {

  public RequestTimeoutException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.REQUEST_TIMEOUT);
  }
}
