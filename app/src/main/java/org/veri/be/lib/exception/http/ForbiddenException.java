package org.veri.be.lib.exception.http;

import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApplicationException {

  public ForbiddenException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.FORBIDDEN);
  }
}
