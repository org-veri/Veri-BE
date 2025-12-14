package org.veri.be.domain.book.client;

import org.veri.be.domain.book.dto.book.NaverBookResponse;

public interface BookSearchClient {

    NaverBookResponse search(String query, int page, int size);
}
