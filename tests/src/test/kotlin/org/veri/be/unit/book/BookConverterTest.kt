package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.domain.book.dto.book.BookResponse
import org.veri.be.domain.book.dto.book.BookSearchResponse
import org.veri.be.domain.book.dto.book.NaverBookItem
import org.veri.be.domain.book.dto.book.NaverBookResponse

class BookConverterTest {

    @Nested
    @DisplayName("toBookResponse")
    inner class ToBookResponse {

        @Test
        @DisplayName("네이버 응답 아이템을 도서 응답으로 변환한다")
        fun mapsNaverItem() {
            val item = NaverBookItem()
            setField(item, "title", "title")
            setField(item, "author", "author")
            setField(item, "image", "https://example.com/book.png")
            setField(item, "publisher", "publisher")
            setField(item, "isbn", "isbn-1")

            val response: BookResponse = BookResponse.from(item)

            assertThat(response.title).isEqualTo("title")
            assertThat(response.author).isEqualTo("author")
            assertThat(response.imageUrl).isEqualTo("https://example.com/book.png")
            assertThat(response.publisher).isEqualTo("publisher")
            assertThat(response.isbn).isEqualTo("isbn-1")
        }
    }

    @Nested
    @DisplayName("toBookSearchResponse")
    inner class ToBookSearchResponse {

        @Test
        @DisplayName("페이지 정보와 결과를 변환한다")
        fun mapsSearchResponse() {
            val item = NaverBookItem()
            setField(item, "title", "title")
            setField(item, "author", "author")
            setField(item, "image", "https://example.com/book.png")
            setField(item, "publisher", "publisher")
            setField(item, "isbn", "isbn-1")

            val response = NaverBookResponse()
            setField(response, "items", listOf(item))
            setField(response, "display", 10)
            setField(response, "start", 11)
            setField(response, "total", 25)

            val result: BookSearchResponse = BookSearchResponse.from(response)

            assertThat(result.books()).hasSize(1)
            assertThat(result.page()).isEqualTo(2)
            assertThat(result.size()).isEqualTo(10)
            assertThat(result.totalElements()).isEqualTo(25)
            assertThat(result.totalPages()).isEqualTo(3)
        }

        @Test
        @DisplayName("display가 0 이하이면 기본값 10으로 계산한다")
        fun defaultsDisplayWhenZero() {
            val response = NaverBookResponse()
            setField(response, "items", listOf<NaverBookItem>())
            setField(response, "display", 0)
            setField(response, "start", 0)
            setField(response, "total", 0)

            val result: BookSearchResponse = BookSearchResponse.from(response)

            assertThat(result.size()).isEqualTo(10)
            assertThat(result.page()).isEqualTo(1)
            assertThat(result.totalPages()).isZero()
        }
    }

    private fun setField(target: Any, name: String, value: Any?) {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(target, value)
    }
}
