package org.veri.be.lib.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApplicationException extends RuntimeException {

  protected final ErrorInfo errorInfo;
  protected final HttpStatus httpStatus;

  public ApplicationException(ErrorInfo errorInfo, HttpStatus httpStatus) {
    super(errorInfo.getMessage());
    this.errorInfo = errorInfo;
    this.httpStatus = httpStatus;
  }
}
