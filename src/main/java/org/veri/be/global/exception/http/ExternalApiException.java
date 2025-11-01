package org.veri.be.global.exception.http;

import org.veri.be.global.exception.ApplicationException;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;

public class ExternalApiException extends ApplicationException {

  public ExternalApiException(ErrorInfo errorInfo) {
    super(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
