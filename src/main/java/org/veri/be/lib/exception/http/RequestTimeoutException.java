package org.veri.be.lib.exception.http;

import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class RequestTimeoutException extends ApplicationException {

  public RequestTimeoutException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.REQUEST_TIMEOUT);
  }
}
