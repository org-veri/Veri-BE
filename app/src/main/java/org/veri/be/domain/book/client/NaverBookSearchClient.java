package org.veri.be.domain.book.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.veri.be.domain.book.config.NaverConfig;
import org.veri.be.domain.book.dto.book.NaverBookResponse;
import org.veri.be.domain.book.exception.BookErrorInfo;
import org.veri.be.lib.exception.http.BadRequestException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class NaverBookSearchClient implements BookSearchClient {

    private final RestTemplate restTemplate;
    private final NaverConfig naverConfig;
    private final ObjectMapper objectMapper;

    @Override
    public NaverBookResponse search(String query, int page, int size) {
        int start = (page - 1) * size + 1;
        if (start > 1000) {
            throw new BadRequestException(BookErrorInfo.BAD_REQUEST);
        }

        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/book.json")
                .queryParam("query", query)
                .queryParam("display", size)
                .queryParam("start", start)
                .queryParam("sort", "sim")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverConfig.getClientId());
        headers.set("X-Naver-Client-Secret", naverConfig.getClientSecret());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        if (respEntity.getStatusCode().is5xxServerError() || respEntity.getBody() == null) {
            throw new BadRequestException(BookErrorInfo.BAD_REQUEST);
        }

        try {
            return objectMapper.readValue(respEntity.getBody(), NaverBookResponse.class);
        } catch (JacksonException _) {
            throw new BadRequestException(BookErrorInfo.BAD_REQUEST);
        }
    }
}
