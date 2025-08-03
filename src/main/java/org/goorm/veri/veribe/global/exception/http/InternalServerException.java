package org.goorm.veri.veribe.global.exception.http;

import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class InternalServerException extends ApplicationException {

  public InternalServerException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
