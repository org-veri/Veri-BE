package org.goorm.veri.veribe.global.exception.http;

import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends ApplicationException {

  public TooManyRequestsException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.TOO_MANY_REQUESTS);
  }
}
