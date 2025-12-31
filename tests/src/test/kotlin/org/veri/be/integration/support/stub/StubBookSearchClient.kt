package org.veri.be.integration.support.stub

import org.springframework.test.util.ReflectionTestUtils
import org.veri.be.book.client.BookSearchClient
import org.veri.be.book.dto.book.NaverBookItem
import org.veri.be.book.dto.book.NaverBookResponse

class StubBookSearchClient : BookSearchClient {
    override fun search(query: String, page: Int, size: Int): NaverBookResponse {
        val response = NaverBookResponse()
        val item = NaverBookItem()
        ReflectionTestUtils.setField(item, "title", "Stub Book Title")
        ReflectionTestUtils.setField(item, "author", "Stub Author")
        ReflectionTestUtils.setField(item, "isbn", "1234567890")
        ReflectionTestUtils.setField(item, "image", "http://stub.image/1.png")

        ReflectionTestUtils.setField(response, "items", listOf(item))
        ReflectionTestUtils.setField(response, "total", 1)
        ReflectionTestUtils.setField(response, "start", 1)
        ReflectionTestUtils.setField(response, "display", 10)
        return response
    }
}
