package org.veri.be.lib.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.handler.GlobalExceptionHandler;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final GlobalExceptionHandler exceptionHelper;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (ApplicationException e) {
            ApiResponse<Map<?, ?>> apiResponse = exceptionHelper.handleApplicationException(e);
            writeErrorResponse(response, apiResponse);
        } catch (Exception e) {
            ApiResponse<Map<?, ?>> apiResponse = exceptionHelper.handleAnyUnexpectedException(e);
            writeErrorResponse(response, apiResponse);
        }
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            ApiResponse<?> apiResponse
    ) throws IOException {
        response.setStatus(apiResponse.getHttpStatus().value());
        response.setContentType(apiResponse.getContentType().toString());
        apiResponse.getHeaders().forEach(header ->
                response.addHeader(header.name(), header.value())
        );

        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
