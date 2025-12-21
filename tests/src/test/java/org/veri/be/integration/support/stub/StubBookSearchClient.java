package org.veri.be.integration.support.stub;

import org.veri.be.domain.book.client.BookSearchClient;
import org.veri.be.domain.book.dto.book.NaverBookItem;
import org.veri.be.domain.book.dto.book.NaverBookResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class StubBookSearchClient implements BookSearchClient {
    @Override
    public NaverBookResponse search(String query, int page, int size) {
        NaverBookResponse response = new NaverBookResponse();
        NaverBookItem item = new NaverBookItem();
        ReflectionTestUtils.setField(item, "title", "Stub Book Title");
        ReflectionTestUtils.setField(item, "author", "Stub Author");
        ReflectionTestUtils.setField(item, "isbn", "1234567890");
        ReflectionTestUtils.setField(item, "image", "http://stub.image/1.png");
        
        ReflectionTestUtils.setField(response, "items", List.of(item));
        ReflectionTestUtils.setField(response, "total", 1);
        ReflectionTestUtils.setField(response, "start", 1);
        ReflectionTestUtils.setField(response, "display", 10);
        return response;
    }
}
