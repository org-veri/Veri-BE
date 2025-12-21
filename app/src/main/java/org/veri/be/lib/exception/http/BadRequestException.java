package org.veri.be.lib.exception.http;

import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApplicationException {

  public BadRequestException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.BAD_REQUEST);
  }
}
