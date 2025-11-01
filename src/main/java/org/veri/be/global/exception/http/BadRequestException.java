package org.veri.be.global.exception.http;

import org.veri.be.global.exception.ApplicationException;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApplicationException {

  public BadRequestException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.BAD_REQUEST);
  }
}
