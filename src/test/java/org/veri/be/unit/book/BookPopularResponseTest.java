package org.veri.be.unit.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.book.dto.book.BookPopularResponse;

class BookPopularResponseTest {

    @Nested
    @DisplayName("record")
    class Record {

        @Test
        @DisplayName("필드 값을 보존한다")
        void preservesValues() {
            BookPopularResponse response = new BookPopularResponse(
                    "https://example.com/book.png",
                    "title",
                    "author",
                    "publisher",
                    "isbn-1"
            );

            assertThat(response.image()).isEqualTo("https://example.com/book.png");
            assertThat(response.title()).isEqualTo("title");
            assertThat(response.author()).isEqualTo("author");
            assertThat(response.publisher()).isEqualTo("publisher");
            assertThat(response.isbn()).isEqualTo("isbn-1");
        }
    }
}
