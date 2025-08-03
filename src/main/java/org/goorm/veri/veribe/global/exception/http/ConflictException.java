package org.goorm.veri.veribe.global.exception.http;

import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class ConflictException extends ApplicationException {

  public ConflictException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.CONFLICT);
  }
}
