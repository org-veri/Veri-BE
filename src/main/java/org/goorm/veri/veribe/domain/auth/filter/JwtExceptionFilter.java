package org.goorm.veri.veribe.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.namul.api.payload.code.DefaultResponseErrorCode;
import org.namul.api.payload.code.dto.supports.DefaultResponseErrorReasonDTO;
import org.namul.api.payload.error.exception.ServerApplicationException;
import org.namul.api.payload.writer.FailureResponseWriter;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final FailureResponseWriter<DefaultResponseErrorReasonDTO> failureResponseWriter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ServerApplicationException e) {
            handleServerApplicationException(response, e);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleServerApplicationException(HttpServletResponse response, ServerApplicationException e) throws IOException {
        response.setStatus(e.getErrorReason().getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), failureResponseWriter.onFailure((DefaultResponseErrorReasonDTO) e.getErrorReason(), null));
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        DefaultResponseErrorReasonDTO reasonDTO = DefaultResponseErrorCode._UNAUTHORIZED.getReason();
        response.setStatus(reasonDTO.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), failureResponseWriter.onFailure(reasonDTO, e.getMessage()));
    }
}
