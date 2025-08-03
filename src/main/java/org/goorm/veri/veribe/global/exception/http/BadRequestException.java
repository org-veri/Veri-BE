package org.goorm.veri.veribe.global.exception.http;

import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApplicationException {

  public BadRequestException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.BAD_REQUEST);
  }
}
