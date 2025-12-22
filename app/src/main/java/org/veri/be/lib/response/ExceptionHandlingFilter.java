package org.veri.be.lib.response;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.handler.GlobalExceptionHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final GlobalExceptionHandler exceptionHelper;
    private final ObjectMapper objectMapper;

    private boolean isBeforeController(HttpServletRequest request) {
        return request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) == null;
    }

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
                ResponseEntity<ProblemDetail> re =
                        exceptionHelper.handleApplicationExceptionBeforeController(e, request);
                writeResponseEntity(response, re);
                return;
            }
            throw e;
        } catch (Exception e) {
            if (isBeforeController(request)) {
                ResponseEntity<ProblemDetail> re =
                        exceptionHelper.handleUnexpectedBeforeController(e, request);
                writeResponseEntity(response, re);
                return;
            }
            throw e;
        }
    }

    private void writeResponseEntity(
            HttpServletResponse response,
            ResponseEntity<ProblemDetail> re
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(re.getStatusCode().value());

        MediaType contentType = re.getHeaders().getContentType();
        response.setContentType(contentType != null ? contentType.toString() : MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), re.getBody());
    }
}
