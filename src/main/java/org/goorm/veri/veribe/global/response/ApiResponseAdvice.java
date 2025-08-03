package org.goorm.veri.veribe.global.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@Component
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (!(body instanceof ApiResponse<?> apiResult)) {
            return body;
        }

        response.setStatusCode(apiResult.getHttpStatus());
        response.getHeaders().addAll(buildHeaders(apiResult));

        if (apiResult.getContentType() == MediaType.APPLICATION_JSON) {
            return apiResult;
        } else {
            return apiResult.getResult();
        }
    }

    private HttpHeaders buildHeaders(ApiResponse<?> apiResult) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(apiResult.getContentType());
        apiResult.getHeaders().forEach(header -> headers.add(header.name(), header.value()));

        return headers;
    }
}
