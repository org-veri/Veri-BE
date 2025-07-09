package org.goorm.veri.veribe.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.namul.api.payload.code.dto.supports.DefaultResponseErrorReasonDTO;
import org.namul.api.payload.handler.ExceptionAdviceHandler;
import org.namul.api.payload.response.BaseResponse;
import org.namul.api.payload.writer.FailureResponseWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Component
@RequiredArgsConstructor
public class NoResourceExceptionHandler implements ExceptionAdviceHandler<NoResourceFoundException, DefaultResponseErrorReasonDTO> {

    private final FailureResponseWriter<DefaultResponseErrorReasonDTO> failureResponseWriter;

    @Override
    public BaseResponse handleException(NoResourceFoundException e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, DefaultResponseErrorReasonDTO dto) {
        return failureResponseWriter.onFailure(dto, "등록된 URL이 아닙니다. " + e.getMessage());
    }
}
