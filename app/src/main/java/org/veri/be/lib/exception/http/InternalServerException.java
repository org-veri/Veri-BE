package org.veri.be.lib.exception.http;

import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class InternalServerException extends ApplicationException {

  public InternalServerException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
