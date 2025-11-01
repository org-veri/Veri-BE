package org.veri.be.global.exception.http;

import org.veri.be.global.exception.ApplicationException;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends ApplicationException {

  public UnAuthorizedException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.UNAUTHORIZED);
  }
}
