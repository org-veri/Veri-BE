package org.veri.be.support.assertion

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.function.Executable
import org.veri.be.lib.exception.ApplicationException
import org.veri.be.lib.exception.ErrorCode

object ExceptionAssertions {
    fun assertApplicationException(
        executable: Executable,
        expected: ErrorCode
    ): ApplicationException {
        val exception = assertThrows(ApplicationException::class.java, executable)
        assertThat(exception.errorCode).isEqualTo(expected)
        return exception
    }
}
