package org.veri.be.book.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.book.dto.book.NaverBookResponse;

@Component
@RequiredArgsConstructor
public class NaverBookClient implements BookSearchClient {

    private final NaverBookSearchClient naverBookSearchClient;

    @Override
    public NaverBookResponse search(String query, int page, int size) {
        return naverBookSearchClient.search(query, page, size);
    }
}
