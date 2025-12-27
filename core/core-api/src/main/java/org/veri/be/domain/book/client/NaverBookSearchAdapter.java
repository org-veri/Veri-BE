package org.veri.be.domain.book.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.domain.book.dto.book.NaverBookResponse;

@Component
@RequiredArgsConstructor
public class NaverBookSearchAdapter implements BookSearchClient {

    private final NaverBookSearchClient naverBookSearchClient;

    @Override
    public NaverBookResponse search(String query, int page, int size) {
        return naverBookSearchClient.search(query, page, size);
    }
}
