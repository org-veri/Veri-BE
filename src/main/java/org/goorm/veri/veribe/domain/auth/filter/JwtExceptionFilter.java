package org.goorm.veri.veribe.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ApplicationException e) {
            handleServerApplicationException(response, e);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleServerApplicationException(HttpServletResponse response, ApplicationException e) throws IOException {
        response.setStatus(e.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> apiResponse = ApiResponse.error(e.getErrorInfo(), e.getHttpStatus());
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> apiResponse = ApiResponse.error(
                CommonErrorInfo.INVALID_TOKEN,
                HttpStatus.UNAUTHORIZED
        );
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
