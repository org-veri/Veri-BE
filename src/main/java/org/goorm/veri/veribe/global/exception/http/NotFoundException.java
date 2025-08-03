package org.goorm.veri.veribe.global.exception.http;

import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {

  public NotFoundException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.NOT_FOUND);
  }
}
