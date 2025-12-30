package org.veri.be.book.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.veri.be.book.config.NaverConfig;
import org.veri.be.book.dto.book.NaverBookResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class NaverBookSearchClient {

    private final RestClient restClient;
    private final NaverConfig naverConfig;
    private final ObjectMapper objectMapper;

    public NaverBookResponse search(String query, int page, int size) {
        int start = (page - 1) * size + 1;
        if (start > 1000) {
            throw new NaverClientException("Search start index exceeds Naver limit.");
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

        String body;
        try {
            body = restClient.get()
                    .uri(uri)
                    .headers(headers -> {
                        headers.set("X-Naver-Client-Id", naverConfig.getClientId());
                        headers.set("X-Naver-Client-Secret", naverConfig.getClientSecret());
                    })
                    .retrieve()
                    .toEntity(String.class)
                    .getBody();
        } catch (RestClientException _) {
            throw new NaverClientException("Naver API returned an error response.");
        }

        if (body == null) {
            throw new NaverClientException("Naver API returned an error response.");
        }

        try {
            return objectMapper.readValue(body, NaverBookResponse.class);
        } catch (JacksonException _) {
            throw new NaverClientException("Failed to parse Naver API response.");
        }
    }
}
