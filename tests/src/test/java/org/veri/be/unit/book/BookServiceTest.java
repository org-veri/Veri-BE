package org.veri.be.unit.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.veri.be.domain.book.client.BookSearchClient;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.service.BookService;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    BookSearchClient bookSearchClient;

    BookService bookService;

    @Captor
    ArgumentCaptor<Book> bookCaptor;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, bookSearchClient);
    }

    @Nested
    @DisplayName("addBook")
    class AddBook {

        @Test
        @DisplayName("이미 존재하면 기존 ID를 반환한다")
        void returnsExistingId() {
            Book existing = Book.builder()
                    .id(1L)
                    .title("title")
                    .author("author")
                    .isbn("isbn-1")
                    .image("https://example.com/book.png")
                    .build();
            given(bookRepository.findBookByIsbn("isbn-1")).willReturn(Optional.of(existing));

            Long result = bookService.addBook("title", "https://example.com/book.png", "author", "publisher", "isbn-1");

            assertThat(result).isEqualTo(1L);
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("존재하지 않으면 신규 도서를 저장한다")
        void savesNewBook() {
            given(bookRepository.findBookByIsbn("isbn-1")).willReturn(Optional.empty());
            given(bookRepository.save(any(Book.class))).willAnswer(invocation -> {
                Book book = invocation.getArgument(0);
                ReflectionTestUtils.setField(book, "id", 2L);
                return book;
            });

            Long result = bookService.addBook("title", "https://example.com/book.png", "author", "publisher", "isbn-1");

            verify(bookRepository).save(bookCaptor.capture());
            Book saved = bookCaptor.getValue();
            assertThat(saved.getTitle()).isEqualTo("title");
            assertThat(saved.getAuthor()).isEqualTo("author");
            assertThat(saved.getPublisher()).isEqualTo("publisher");
            assertThat(saved.getIsbn()).isEqualTo("isbn-1");
            assertThat(result).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("getBookById")
    class GetBookById {

        @Test
        @DisplayName("bookId가 null이면 null을 반환한다")
        void returnsNullWhenIdNull() {
            Book result = bookService.getBookById(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("존재하지 않으면 NotFoundException을 던진다")
        void throwsWhenNotFound() {
            given(bookRepository.findById(1L)).willReturn(Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> bookService.getBookById(1L),
                    CommonErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    @Nested
    @DisplayName("searchBook")
    class SearchBook {

        @Test
        @DisplayName("검색 결과를 변환해 반환한다")
        void returnsSearchResponse() {
            org.veri.be.domain.book.dto.book.NaverBookItem item =
                    new org.veri.be.domain.book.dto.book.NaverBookItem();
            ReflectionTestUtils.setField(item, "title", "title");
            ReflectionTestUtils.setField(item, "author", "author");
            ReflectionTestUtils.setField(item, "image", "https://example.com/book.png");
            ReflectionTestUtils.setField(item, "publisher", "publisher");
            ReflectionTestUtils.setField(item, "isbn", "isbn-1");

            org.veri.be.domain.book.dto.book.NaverBookResponse naverResponse =
                    new org.veri.be.domain.book.dto.book.NaverBookResponse();
            ReflectionTestUtils.setField(naverResponse, "items", java.util.List.of(item));
            ReflectionTestUtils.setField(naverResponse, "total", 1);
            ReflectionTestUtils.setField(naverResponse, "start", 1);
            ReflectionTestUtils.setField(naverResponse, "display", 10);

            given(bookSearchClient.search("query", 1, 10)).willReturn(naverResponse);

            org.veri.be.domain.book.dto.book.BookSearchResponse result =
                    bookService.searchBook("query", 1, 10);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().get(0).getTitle()).isEqualTo("title");
            assertThat(result.books().get(0).getAuthor()).isEqualTo("author");
        }
    }
}
