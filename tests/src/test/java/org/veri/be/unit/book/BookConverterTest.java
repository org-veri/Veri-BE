package org.veri.be.unit.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.book.dto.book.BookConverter;
import org.veri.be.domain.book.dto.book.BookSearchResponse;
import org.veri.be.domain.book.dto.book.BookResponse;
import org.veri.be.domain.book.dto.book.NaverBookItem;
import org.veri.be.domain.book.dto.book.NaverBookResponse;

class BookConverterTest {

    @Nested
    @DisplayName("toBookResponse")
    class ToBookResponse {

        @Test
        @DisplayName("네이버 응답 아이템을 도서 응답으로 변환한다")
        void mapsNaverItem() throws Exception {
            NaverBookItem item = new NaverBookItem();
            setField(item, "title", "title");
            setField(item, "author", "author");
            setField(item, "image", "https://example.com/book.png");
            setField(item, "publisher", "publisher");
            setField(item, "isbn", "isbn-1");

            BookResponse response = BookConverter.toBookResponse(item);

            assertThat(response.getTitle()).isEqualTo("title");
            assertThat(response.getAuthor()).isEqualTo("author");
            assertThat(response.getImageUrl()).isEqualTo("https://example.com/book.png");
            assertThat(response.getPublisher()).isEqualTo("publisher");
            assertThat(response.getIsbn()).isEqualTo("isbn-1");
        }
    }

    @Nested
    @DisplayName("toBookSearchResponse")
    class ToBookSearchResponse {

        @Test
        @DisplayName("페이지 정보와 결과를 변환한다")
        void mapsSearchResponse() throws Exception {
            NaverBookItem item = new NaverBookItem();
            setField(item, "title", "title");
            setField(item, "author", "author");
            setField(item, "image", "https://example.com/book.png");
            setField(item, "publisher", "publisher");
            setField(item, "isbn", "isbn-1");

            NaverBookResponse response = new NaverBookResponse();
            setField(response, "items", List.of(item));
            setField(response, "display", 10);
            setField(response, "start", 11);
            setField(response, "total", 25);

            BookSearchResponse result = BookConverter.toBookSearchResponse(response);

            assertThat(result.books()).hasSize(1);
            assertThat(result.page()).isEqualTo(2);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(25);
            assertThat(result.totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("display가 0 이하이면 기본값 10으로 계산한다")
        void defaultsDisplayWhenZero() throws Exception {
            NaverBookResponse response = new NaverBookResponse();
            setField(response, "items", List.of());
            setField(response, "display", 0);
            setField(response, "start", 0);
            setField(response, "total", 0);

            BookSearchResponse result = BookConverter.toBookSearchResponse(response);

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.page()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(0);
        }
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
