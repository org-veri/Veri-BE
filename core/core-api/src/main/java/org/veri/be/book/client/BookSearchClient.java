package org.veri.be.book.client;

import org.veri.be.book.dto.book.NaverBookResponse;

public interface BookSearchClient {

    NaverBookResponse search(String query, int page, int size);
}
