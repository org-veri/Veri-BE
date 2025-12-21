package org.veri.be.lib.response;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.handler.GlobalExceptionHandler;
import tools.jackson.databind.ObjectMapper;

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
    ) throws IOException, ServletException {

        try {
            filterChain.doFilter(request, response);
        } catch (ApplicationException e) {
            if (isBeforeController(request)) {
                ApiResponse<Map<?, ?>> apiResponse = exceptionHelper.handleApplicationException(e);
                writeErrorResponseIfPossible(response, apiResponse);
            } else {
                throw e;
            }
        } catch (Exception e) {
            if (isBeforeController(request)) {
                ApiResponse<Map<?, ?>> apiResponse = exceptionHelper.handleAnyUnexpectedException(e);
                writeErrorResponseIfPossible(response, apiResponse);
            } else {
                throw e;
            }
        }
    }

    private boolean isBeforeController(HttpServletRequest request) {
        return request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) == null;
    }

    private void writeErrorResponseIfPossible(
            HttpServletResponse response,
            ApiResponse<?> apiResponse
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.resetBuffer();
        response.setStatus(apiResponse.getHttpStatus().value());
        response.setContentType(apiResponse.getContentType().toString());
        apiResponse.getHeaders().forEach(header ->
                response.addHeader(header.name(), header.value())
        );
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
