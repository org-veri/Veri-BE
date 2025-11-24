package org.veri.be.lib.exception.http;

import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {

  public NotFoundException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.NOT_FOUND);
  }
}
