package org.veri.be.support.assertion;

import org.junit.jupiter.api.function.Executable;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.ErrorInfo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ExceptionAssertions {

    private ExceptionAssertions() {
    }

    public static ApplicationException assertApplicationException(
            Executable executable,
            ErrorInfo expected
    ) {
        ApplicationException exception = assertThrows(ApplicationException.class, executable);
        assertThat(exception.getErrorInfo()).isEqualTo(expected);
        return exception;
    }
}
