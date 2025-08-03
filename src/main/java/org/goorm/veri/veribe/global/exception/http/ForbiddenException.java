package org.goorm.veri.veribe.global.exception.http;

import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApplicationException {

  public ForbiddenException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.FORBIDDEN);
  }
}
