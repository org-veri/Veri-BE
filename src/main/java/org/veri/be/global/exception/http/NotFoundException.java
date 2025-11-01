package org.veri.be.global.exception.http;

import org.veri.be.global.exception.ApplicationException;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {

  public NotFoundException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.NOT_FOUND);
  }
}
